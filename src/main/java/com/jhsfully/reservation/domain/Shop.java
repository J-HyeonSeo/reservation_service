package com.jhsfully.reservation.domain;

import com.jhsfully.reservation.model.ShopTopResponse;
import com.jhsfully.reservation.type.Days;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "shop")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    @Column(unique = true, nullable = false)
    private String name;
    private String introduce;
    private double star;
    private long starSum;
    private long reviewCount;
    private String address;
    private double latitude;
    private double longitude;
    private int resOpenWeek; //예약을 몇 주 이후까지 받을 것인가?
    private int resOpenCount; //동시간대에 예약 가능한 인원
    @ElementCollection
    @CollectionTable(name = "res_open_day", joinColumns = @JoinColumn(name = "shop_id"))
    @Column(name = "open_day")
    @Enumerated(EnumType.STRING)
    private List<Days> resOpenDays;
    @ElementCollection
    @CollectionTable(name = "res_open_time", joinColumns = @JoinColumn(name = "shop_id"))
    @Column(name = "open_time")
    private List<LocalTime> resOpenTimes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;

    public void subStar(long star){
        if(star <= 0){
            return;
        }
        if(this.starSum - star < 0){
            return;
        }

        this.starSum -= star;
        this.reviewCount -= 1;
    }
    public void addStar(long star){
        if(star <= 0){
            return;
        }
        this.starSum += star;
        this.reviewCount += 1;
    }
    public void calculateStar(){
        if(this.reviewCount == 0){
            this.star = 0;
            return;
        }
        this.star = this.starSum / (double)this.reviewCount;
    }

    public static ShopTopResponse toTopResponse(Shop shop){

        return ShopTopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .introduce(shop.getIntroduce())
                .distance(0)
                .address(shop.getAddress())
                .star(shop.getStar())
                .build();
    }

}
