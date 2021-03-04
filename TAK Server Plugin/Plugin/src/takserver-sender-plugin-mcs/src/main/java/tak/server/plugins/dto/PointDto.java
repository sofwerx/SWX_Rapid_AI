package tak.server.plugins.dto;

import com.google.gson.annotations.SerializedName;

public class PointDto {
	
    @SerializedName("lat")
	private Double _lat = 0.0;
	
	@SerializedName("lon")
	private Double _lon = 0.0;
	
	@SerializedName("hae")
	private Double _hae = 0.0;
	
	@SerializedName("ce")
	private Double _ce = 0.0;
	
	@SerializedName("le")
	private Double _le = 0.0;
	
	public Double getLat() {
        return _lat;
    }

    public void setLat(Double lat) {
       _lat = lat;
    }
    
    public Double getLon() {
        return _lon;
    }

    public void setLon(Double lon) {
       _lon = lon;
    }
    
    public Double getHae() {
        return _hae;
    }

    public void setHae(Double hae) {
       _hae = hae;
    }
    
    public Double getCe() {
        return _ce;
    }

    public void setCe(Double ce) {
       _ce = ce;
    }
	
    public Double getLe() {
        return _le;
    }

    public void setLe(Double le) {
       _le = le;
    }
}
