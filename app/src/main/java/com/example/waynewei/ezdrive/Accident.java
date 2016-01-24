package com.example.waynewei.ezdrive;

/**
 * Created by waynewei on 2016/1/23.
 */
public class Accident {

	private String description;
	private Position position;

	private long timestamp;
	private int type;

	public Accident(){

	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setPosition(double lat, double lng){
		position = new Position(lat, lng);
	}

	public Position getPosition(){
		return position;
	}


	public void setTimestamp(long timestamp){
		this.timestamp = timestamp;
	}

	public long getTimestamp(){
		return timestamp;
	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}

	public class Position {
		private double lat;
		private double lng;

		public Position(){

		}

		public Position(double lat, double lng){
			this.lat = lat;
			this.lng = lng;
		}

		public void setLat(double lat){
			this.lat = lat;
		}

		public double getLat(){
			return lat;
		}

		public void setLng(double lng){
			this.lng = lng;
		}

		public double getLng(){
			return lng;
		}

	}

}
