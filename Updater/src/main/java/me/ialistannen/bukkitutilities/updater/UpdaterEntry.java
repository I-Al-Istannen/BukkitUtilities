package me.ialistannen.bukkitutilities.updater;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An entry in the updater. Represents some version, past, present or future.
 */
public class UpdaterEntry {

    private String name;
    private String version;
    private LocalDateTime releaseTime;
    private URL downloadUrl;

    /**
     * @param name The name of the file
     * @param version The version of the file
     * @param releaseTime The time it was released
     * @param downloadUrl The URL you can download it from
     */
    public UpdaterEntry(String name, String version, LocalDateTime releaseTime, URL downloadUrl) {
        this.name = name;
        this.version = version;
        this.releaseTime = releaseTime;
        this.downloadUrl = downloadUrl;
    }

    /**
     * @return The name of the file
     */
    public String getName() {
        return name;
    }

    /**
     * @return The version of the file
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return The time it was released
     */
    public LocalDateTime getReleaseTime() {
        return releaseTime;
    }

    /**
     * @return The url you can download it from
     */
    public URL getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public String toString() {
        return "UpdaterEntry{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", releaseTime=" + releaseTime +
                ", downloadUrl=" + downloadUrl +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UpdaterEntry)) {
            return false;
        }
        UpdaterEntry entry = (UpdaterEntry) o;
        return Objects.equals(name, entry.name) &&
                Objects.equals(version, entry.version) &&
                Objects.equals(releaseTime, entry.releaseTime) &&
                Objects.equals(downloadUrl, entry.downloadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, releaseTime, downloadUrl);
    }
}
