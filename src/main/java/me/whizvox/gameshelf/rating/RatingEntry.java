package me.whizvox.gameshelf.rating;

import org.springframework.data.mongodb.core.mapping.DocumentReference;

public class RatingEntry {

  public int order;

  @DocumentReference
  public Rating rating;

  public RatingEntry() {
  }

  public RatingEntry(int order, Rating rating) {
    this.order = order;
    this.rating = rating;
  }

}
