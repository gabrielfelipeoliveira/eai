package com.eai.application.media;

public interface MediaStoragePort {

    StoredMedia store(StoreMediaCommand command);

    MediaObject read(String provider, String key);
}
