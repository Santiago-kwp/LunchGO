ALTER TABLE Reviews
    ADD CONSTRAINT FK_Restaurants_TO_Reviews_1
        FOREIGN KEY (restaurant_id) REFERENCES Restaurants (restaurant_id) ON DELETE CASCADE;

ALTER TABLE `Reviews` ADD CONSTRAINT `FK_Users_TO_Reviews_1` FOREIGN KEY (
                                                                          `user_id`
    )
    REFERENCES `Users` (
                        `user_id`
        );

ALTER TABLE `Reviews` ADD CONSTRAINT `FK_Receipts_TO_Reviews_1` FOREIGN KEY (
                                                                             `receipt_id`
    )
    REFERENCES `Receipts` (
                           `receipt_id`
        );


ALTER TABLE menu_tag_maps
    ADD CONSTRAINT fk_menu_tag_map_menu
        FOREIGN KEY (menu_id) REFERENCES menus(menu_id) ON DELETE CASCADE;


