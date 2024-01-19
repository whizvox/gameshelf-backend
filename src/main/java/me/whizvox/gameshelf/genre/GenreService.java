package me.whizvox.gameshelf.genre;

import me.whizvox.gameshelf.util.ArgumentsUtils;
import me.whizvox.gameshelf.util.ServiceUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class GenreService {

  private final GenreRepository genreRepo;

  @Autowired
  public GenreService(GenreRepository genreRepo) {
    this.genreRepo = genreRepo;
  }

  public Optional<Genre> findById(ObjectId id) {
    return genreRepo.findById(id);
  }

  public Optional<Genre> findByName(String name) {
    return genreRepo.findByName(name);
  }

  public List<Genre> findAll() {
    return genreRepo.findAll();
  }

  public List<Genre> findAllNonExplicit() {
    return genreRepo.findAllNonExplicit();
  }

  public boolean isNameAvailable(String name) {
    return findByName(name).isEmpty();
  }

  public Genre create(String name, @Nullable String description, @Nullable Boolean explicit) {
    ServiceUtils.checkUnique(this::isNameAvailable, name, "name");
    Genre genre = new Genre(name, description, Objects.requireNonNullElse(explicit, false));
    return genreRepo.save(genre);
  }

  public Genre update(ObjectId id, MultiValueMap<String, String> args) {
    Genre genre = ServiceUtils.getOrNotFound(genreRepo::findById, id, Genre.class);
    ArgumentsUtils.getString(args, "name", value -> {
      ServiceUtils.checkUnique(this::isNameAvailable, value, "name");
      genre.name = value;
    });
    ArgumentsUtils.getString(args, "description", value -> genre.description = value);
    ArgumentsUtils.getBoolean(args, "explicit", value -> genre.explicit = value);
    return genreRepo.save(genre);
  }

  public void delete(ObjectId id) {
    genreRepo.deleteById(id);
  }

}
