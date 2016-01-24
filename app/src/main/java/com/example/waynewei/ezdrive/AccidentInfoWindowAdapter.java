package com.example.waynewei.ezdrive;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by waynewei on 2016/1/24.
 */
public class AccidentInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

	private Context mContext;
	private final View view;

	AccidentInfoWindowAdapter(Context context, Accident accident){
		 mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		view = inflater.inflate( R.layout.info_window, null );
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	@Override
	public View getInfoContents(Marker marker) {

		String[] content = marker.getSnippet().split("-");

		TextView mTitle = ((TextView) view.findViewById(R.id.title));
			mTitle.setText(marker.getTitle());
			switch (Integer.valueOf(content[0])){
				case 0:
					mTitle.setTextColor(mContext.getResources().getColor(R.color.md_red_900));
					break;
				case 1:
					mTitle.setTextColor(mContext.getResources().getColor(R.color.md_amber_900));
					break;
				case 2:
					mTitle.setTextColor(mContext.getResources().getColor(R.color.md_light_green_900));
					break;
				case 3:
					mTitle.setTextColor(mContext.getResources().getColor(R.color.md_cyan_900));
					break;
			}



		TextView mDescription = ((TextView) view.findViewById(R.id.description));
		if(!content[1].isEmpty()) {
			mDescription.setVisibility(View.VISIBLE);
			mDescription.setText("路況: " + content[1]);
		}
		else
			mDescription.setVisibility(View.GONE);

		TextView mTimeStamp = ((TextView) view.findViewById(R.id.timestamp));

		if(Long.valueOf(content[2])!=0){
			mTimeStamp.setVisibility(View.VISIBLE);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date(Long.valueOf(content[2]));
			mTimeStamp.setText("更新日期: " + sdf.format(date));
		}
		else
			mTimeStamp.setVisibility(View.GONE);

		return view;
	}

}
