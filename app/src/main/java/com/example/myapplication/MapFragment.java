package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_HAS_FOCUS = "has_focus";
    private static final String ARG_FOCUS_LAT = "focus_lat";
    private static final String ARG_FOCUS_LNG = "focus_lng";
    private static final String ARG_FOCUS_TITLE = "focus_title";

    private static final String DIRECTIONS_API_KEY = "AIzaSyBtv-OGUaRDNrDAQaPRnkYTNpVJ2sfL1k8";

    private GoogleMap map;
    private final HashMap<String, Marker> markerMap = new HashMap<>();

    private FusedLocationProviderClient fusedClient;

    private MaterialButton btnNavigate;
    private MaterialButton btnDirections;

    private Marker selectedMarker;
    private Polyline currentRoute;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableMyLocationLayer();
                    zoomToMyLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    public static MapFragment newInstanceFocus(double lat, double lng, String title) {
        MapFragment f = new MapFragment();
        Bundle b = new Bundle();
        b.putBoolean(ARG_HAS_FOCUS, true);
        b.putDouble(ARG_FOCUS_LAT, lat);
        b.putDouble(ARG_FOCUS_LNG, lng);
        b.putString(ARG_FOCUS_TITLE, title);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnNavigate = view.findViewById(R.id.btnNavigate);
        btnDirections = view.findViewById(R.id.btnDirections);

        if (btnDirections != null) {
            btnDirections.setVisibility(View.GONE);
            btnDirections.setOnClickListener(v -> onDirectionsClicked());
        }

        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(v -> requestLocationThenZoom());
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        // ✅ STEP 1: Tap RED pin => ONLY show bubble with ALL info
        map.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;

            marker.showInfoWindow();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18f));

            if (btnDirections != null) btnDirections.setVisibility(View.VISIBLE);

            // return true = we handled it, do NOT open details here
            return true;
        });

        // ✅ STEP 2: Tap the bubble => open details screen
        map.setOnInfoWindowClickListener(marker -> openDetailsFromMarker(marker));

        // Tap empty map => hide directions button + clear selection
        map.setOnMapClickListener(latLng -> {
            selectedMarker = null;
            if (btnDirections != null) btnDirections.setVisibility(View.GONE);

            // Optional: remove route when you click empty space
            // if (currentRoute != null) { currentRoute.remove(); currentRoute = null; }
        });

        loadMarkersFromDb();

        if (hasLocationPermission()) {
            enableMyLocationLayer();
        }
    }

    private void openDetailsFromMarker(Marker marker) {
        Object tag = marker.getTag();
        if (!(tag instanceof PlaceEntity)) return;

        PlaceEntity e = (PlaceEntity) tag;

        // Get username safely
        String username = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("username", "");

        // ✅ Run DB query on background thread (prevents crash)
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean isFav = false;

            try {
                isFav = AppDatabase.getInstance(requireContext())
                        .favoriteDao()
                        .isFavorite(username, e.id) == 1;
            } catch (Exception ex) {
                // optional: log ex
            }

            boolean finalIsFav = isFav;

            if (!isAdded() || getActivity() == null) return;

            requireActivity().runOnUiThread(() -> {
                if (!isAdded() || getActivity() == null) return;

                Place p = new Place(
                        e.id,
                        e.title, e.category, e.description,
                        e.location, e.phone, e.email,
                        e.lat, e.lng,
                        finalIsFav
                );

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, PlaceDetailsFragment.newInstance(p))
                        .addToBackStack(null)
                        .commit();
            });
        });
    }



    private void requestLocationThenZoom() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        enableMyLocationLayer();
        zoomToMyLocation();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocationLayer() {
        if (map == null) return;
        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException ignored) {}
    }

    private void zoomToMyLocation() {
        if (map == null) return;
        try {
            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 18f));
                } else {
                    Toast.makeText(requireContext(),
                            "Не можам да земам локација (емулаторот често нема локација).",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException ignored) {}
    }

    private void onDirectionsClicked() {
        if (selectedMarker == null) {
            Toast.makeText(requireContext(), "Прво избери pin.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        try {
            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                if (location == null) {
                    Toast.makeText(requireContext(),
                            "Нема current location (провери GPS/емулатор location).",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                LatLng dest = selectedMarker.getPosition();
                fetchAndDrawRoute(origin, dest);
            });
        } catch (SecurityException ignored) {}
    }

    private void fetchAndDrawRoute(LatLng origin, LatLng dest) {
        if (currentRoute != null) {
            currentRoute.remove();
            currentRoute = null;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String urlStr =
                        "https://maps.googleapis.com/maps/api/directions/json"
                                + "?origin=" + origin.latitude + "," + origin.longitude
                                + "&destination=" + dest.latitude + "," + dest.longitude
                                + "&mode=walking"
                                + "&key=" + DIRECTIONS_API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream()
                ));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "");

                if (!"OK".equals(status)) {
                    String err = json.optString("error_message", status);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Directions API error: " + err,
                                    Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                JSONArray routes = json.getJSONArray("routes");
                JSONObject route0 = routes.getJSONObject(0);
                JSONObject overviewPolyline = route0.getJSONObject("overview_polyline");
                String encoded = overviewPolyline.getString("points");

                ArrayList<LatLng> points = decodePoly(encoded);

                requireActivity().runOnUiThread(() -> {
                    if (map == null) return;

                    if (currentRoute != null) {
                        currentRoute.remove();
                        currentRoute = null;
                    }

                    currentRoute = map.addPolyline(
                            new PolylineOptions()
                                    .addAll(points)
                                    .color(0xFFFF0000)
                                    .width(10f)
                    );

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 16f));
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Не успеав да земам насоки: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    private void loadMarkersFromDb() {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            List<PlaceEntity> entities = db.placeDao().getAll();

            requireActivity().runOnUiThread(() -> {
                if (map == null) return;

                markerMap.clear();
                map.clear();

                for (PlaceEntity e : entities) {
                    LatLng pos = new LatLng(e.lat, e.lng);

                    // ✅ MULTI-LINE bubble text with ALL info
                    String snippet =
                            "Category: " + safe(e.category) + "\n" +
                                    "Description: " + safe(e.description) + "\n" +
                                    "Location: " + safe(e.location) + "\n" +
                                    "Phone: " + safe(e.phone) + "\n" +
                                    "Email: " + safe(e.email);

                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(safe(e.title))
                            .snippet(snippet));

                    if (marker != null) {
                        marker.setTag(e); // keep full object for opening details
                        if (e.title != null) markerMap.put(e.title, marker);
                    }
                }

                boolean hasFocus = getArguments() != null && getArguments().getBoolean(ARG_HAS_FOCUS, false);

                if (hasFocus) {
                    double lat = getArguments().getDouble(ARG_FOCUS_LAT);
                    double lng = getArguments().getDouble(ARG_FOCUS_LNG);
                    String title = getArguments().getString(ARG_FOCUS_TITLE);

                    LatLng target = new LatLng(lat, lng);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 18f));

                    Marker focused = markerMap.get(title);
                    if (focused != null) {
                        selectedMarker = focused;
                        focused.showInfoWindow();
                        if (btnDirections != null) btnDirections.setVisibility(View.VISIBLE);
                    }

                } else if (!entities.isEmpty()) {
                    PlaceEntity first = entities.get(0);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(first.lat, first.lng), 16f));
                }
            });
        });
    }

    private String safe(String s) {
        if (s == null) return "";
        s = s.trim();
        return s.isEmpty() ? "" : s;
    }
}
