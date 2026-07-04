package kr.co.dearbloom.domain.customer.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.Artist;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_saved_artist_customer_artist",
        columnNames = {"customer_id", "artist_id"}))
public class SavedArtist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedArtistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;
}
