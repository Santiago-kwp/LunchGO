package com.example.LunchGo.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuSnapshot {
    private Long menuId;
    private String menuName;
    private Integer unitPrice;
    private Integer quantity;
    private Integer lineAmount;
}
