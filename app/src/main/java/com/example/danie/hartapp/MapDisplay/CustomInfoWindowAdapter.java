package com.example.danie.hartapp.MapDisplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.danie.hartapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by ttutt on 7/8/2018.
 *
 * Class is constructed, however currently commented out due to
 * implementation setbacks.
 *
 * <P>This adapter is responsible for changing the format of displayed information.
 * Information is displayed in a List format, allowing for the ability to show more
 * information in a more readable manner.</P>
 *
 * @see com.example.danie.hartapp.MapDisplay.MapPresenter#createSnippet()
 */


//public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

//    private final View mWindow;
//    private Context mContext;
//
//    public CustomInfoWindowAdapter(Context context){
//        mContext = context;
//        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
//    }
//
//    private void rendowWindowText(Marker marker, View view){
//
//        String title = marker.getTitle();
//        TextView tvTitle = (TextView) view.findViewById(R.id.snippet);
//
//        if(!title.equals("")){
//            tvTitle.setText(title);
//        }
//
//        String snippet = marker.getSnippet();
//        TextView tvSippet = (TextView) view.findViewById(R.id.snippet);
//
//        if(!snippet.equals("")){
//            tvSippet.setText(snippet);
//        }
////
////    }
//    @Override
//    public View getInfoWindow(Marker marker) {
//        rendowWindowText(marker,mWindow);
//        return mWindow;
//    }
//
//    @Override
//    public View getInfoContents(Marker marker) {
//        rendowWindowText(marker,mWindow);
//        return mWindow;
//    }
//}