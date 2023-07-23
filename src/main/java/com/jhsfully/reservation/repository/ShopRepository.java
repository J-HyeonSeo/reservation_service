package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Shop;
import com.jhsfully.reservation.model.ShopTopResponseInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    /*
        유저가 검색을 하기 위한, SQL문임
        거리 계산을 쉽게 하기 위해서, Native Query로 작성되었음.
     */
    @Query(
            value = "SELECT count(*) over () as shopCount, id, name, introduce, address, st_distance_sphere(point(longitude, latitude), point(:longitudeOrigin, :latitudeOrigin)) as distance, star\n" +
                    "FROM shop\n" +
                    "WHERE name like :searchValue AND is_deleted = false \n" +
                    "ORDER BY\n" +
                        "CASE WHEN :sortingType = 'TEXT' and :isAsc = True THEN name END asc,\n" +
                        "CASE WHEN :sortingType = 'TEXT' and :isAsc = False THEN name END desc,\n" +
                        "CASE WHEN :sortingType = 'STAR' and :isAsc = True THEN star END asc,\n" +
                        "CASE WHEN :sortingType = 'STAR' and :isAsc = False THEN star END desc,\n" +
                        "CASE WHEN :sortingType = 'DISTANCE' and :isAsc = True THEN distance END asc,\n" +
                        "CASE WHEN :sortingType = 'DISTANCE' and :isAsc = False THEN distance END desc " +
                    "LIMIT :start, :end", nativeQuery = true
    )
    List<ShopTopResponseInterface> findByNameAndOrdering(
            @Param("searchValue") String searchValue,
            @Param("latitudeOrigin") double latitude,
            @Param("longitudeOrigin") double longitude,
            @Param("sortingType") String sortingType,
            @Param("isAsc") boolean isAsc,
            @Param("start") long start, @Param("end") long end);

//    List<Shop> findByNameStartingWith(String name);

    Page<Shop> findByMemberAndIsDeletedFalse(Member member, Pageable pageable);

}
