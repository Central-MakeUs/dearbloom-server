package kr.co.dearbloom.domain.product.repository;

import kr.co.dearbloom.domain.artist.entity.Artist;
import kr.co.dearbloom.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByArtist(Artist artist);
}
