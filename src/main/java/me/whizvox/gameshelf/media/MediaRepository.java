package me.whizvox.gameshelf.media;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MediaRepository extends MongoRepository<Media, String> {

}
