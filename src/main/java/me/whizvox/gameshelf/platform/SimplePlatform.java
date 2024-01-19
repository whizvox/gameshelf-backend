package me.whizvox.gameshelf.platform;

import org.bson.types.ObjectId;

import java.util.Objects;

public class SimplePlatform {

  public ObjectId id;

  public String shortName;

  public String name;

  public SimplePlatform() {
  }

  public SimplePlatform(ObjectId id, String shortName, String name) {
    this.id = id;
    this.shortName = shortName;
    this.name = name;
  }

  public SimplePlatform(Platform platform) {
    this(platform.id, platform.shortName, platform.name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SimplePlatform that = (SimplePlatform) o;
    return Objects.equals(id, that.id) && Objects.equals(shortName, that.shortName) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, shortName, name);
  }

  @Override
  public String toString() {
    return "SimplePlatform{" +
        "id=" + id +
        ", shortName='" + shortName + '\'' +
        ", name='" + name + '\'' +
        '}';
  }

}
