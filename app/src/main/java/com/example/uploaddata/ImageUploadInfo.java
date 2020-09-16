package com.example.uploaddata;

public class ImageUploadInfo {

    public String imageName;
    public String animalType;
    public String imageURL;

    public ImageUploadInfo() {

    }

    public ImageUploadInfo(String imageName, String animalType, String imageURL) {
        this.imageName = imageName;
        this.animalType = animalType;
        this.imageURL = imageURL;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageName() {
        return imageName;
    }

    public String getAnimalType() {
        return animalType;
    }

    public String getImageURL() {
        return imageURL;
    }

}
