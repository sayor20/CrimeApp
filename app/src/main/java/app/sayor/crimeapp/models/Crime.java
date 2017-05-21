package app.sayor.crimeapp.models;

/**
 * Created by sayor on 3/23/2017.
 */

// model class for JSON data from server
public class Crime {

    private String beat;
    private String block;
    private String rdNo;
    private String communityArea;
    private String dateOccurred;
    private String iucrDescription;
    private String cpdDistrict;
    private String iucr;
    private String lastUpdated;
    private String locationDesc;
    private String primary;
    private String ward;
    private String xCoordinate;
    private String yCoordinate;

    public String getBeat() {
        return beat;
    }

    public void setBeat(String beat) {
        this.beat = beat;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getRdNo() {
        return rdNo;
    }

    public void setRdNo(String rdNo) {
        this.rdNo = rdNo;
    }

    public String getCommunityArea() {
        return communityArea;
    }

    public void setCommunityArea(String communityArea) {
        this.communityArea = communityArea;
    }

    public String getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(String dateOccurred) {
        this.dateOccurred = dateOccurred;
    }

    public String getIucrDescription() {
        return iucrDescription;
    }

    public void setIucrDescription(String iucrDescription) {
        this.iucrDescription = iucrDescription;
    }

    public String getCpdDistrict() {
        return cpdDistrict;
    }

    public void setCpdDistrict(String cpdDistrict) {
        this.cpdDistrict = cpdDistrict;
    }

    public String getIucr() {
        return iucr;
    }

    public void setIucr(String iucr) {
        this.iucr = iucr;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        this.locationDesc = locationDesc;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(String xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public String getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(String yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public String toString(){

        return new StringBuilder()
                .append(getBeat())
                .append(getBlock())
                .append(getRdNo())
                .append(getCommunityArea())
                .append(getCpdDistrict())
                .append(getDateOccurred())
                .append(getIucr())
                .append(getIucrDescription())
                .append(getLastUpdated())
                .append(getLocationDesc())
                .append(getPrimary())
                .append(getWard()).toString();
    }

}
