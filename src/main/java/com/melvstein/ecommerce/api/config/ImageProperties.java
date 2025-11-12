package com.melvstein.ecommerce.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image")
@Getter
@Setter
public class ImageProperties {
    private int maxWidth = 800;
    private int maxHeight = 800;
    private int maxSizeKb = 100;
    private float minQuality = 0.1f;

    // Getters and setters
    /*public int getMaxWidth() { return maxWidth; }
    public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }

    public int getMaxHeight() { return maxHeight; }
    public void setMaxHeight(int maxHeight) { this.maxHeight = maxHeight; }

    public int getMaxSizeKb() { return maxSizeKb; }
    public void setMaxSizeKb(int maxSizeKb) { this.maxSizeKb = maxSizeKb; }

    public float getMinQuality() { return minQuality; }
    public void setMinQuality(float minQuality) { this.minQuality = minQuality; }*/
}
