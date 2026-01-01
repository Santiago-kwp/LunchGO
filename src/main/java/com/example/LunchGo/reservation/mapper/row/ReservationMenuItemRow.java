package com.example.LunchGo.reservation.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationMenuItemRow {
    private String name;
    private Integer quantity;
    private Integer unitPrice;
    private Integer lineAmount;
}
