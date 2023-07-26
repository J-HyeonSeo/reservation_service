package com.jhsfully.reservation.service;

import com.jhsfully.reservation.model.ShopDto;
import com.jhsfully.reservation.model.ShopTopResponseInterface;

import java.time.LocalDate;
import java.util.List;

public interface ShopService {

    Long addShop(Long memberId, ShopDto.AddShopRequest request);

    void updateShop(Long memberId, Long shopId, ShopDto.AddShopRequest request);

    void deleteShop(Long memberId, Long shopId);

    List<ShopTopResponseInterface> searchShops(ShopDto.SearchShopParam param, int pageIndex);

    List<ShopDto.ShopTopResponse> getShopsByPartner(Long memberId, int pageIndex);

    ShopDto.ShopDetailPartnerResponse getShopDetailForPartner(Long memberId, Long shopId);

    ShopDto.ShopDetailUserResponse getShopDetailForUser(Long shopId, LocalDate dateNow);

    void addShopStar(Long shopId, int star);
    void subShopStar(Long shopId, int star);
    void updateShopStar(Long shopId, int originStar, int newStar);

}
