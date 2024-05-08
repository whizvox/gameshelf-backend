package me.whizvox.gameshelf.media;

import java.util.List;

public class MediaInfo extends Media {

  public String url;

  public MediaInfo(String id, long size, String mimeType, String origFileName, String filePath, String altText, List<String> tags, String url) {
    super(id, size, mimeType, origFileName, filePath, altText, tags);
    this.url = url;
  }

  public MediaInfo(Media media, String url) {
    this(media.id, media.size, media.mimeType, media.origFileName, media.filePath, media.altText, media.tags, url);
  }

  public MediaInfo() {
    url = null;
  }

}
