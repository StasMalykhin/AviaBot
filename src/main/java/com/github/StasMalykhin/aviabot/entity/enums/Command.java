package com.github.StasMalykhin.aviabot.entity.enums;

/**
 * @author Stanislav Malykhin
 */
public enum Command {
    DEPARTURE_CITY_WAS_FOUND_CORRECTLY("/departure_city_was_found_correctly"),
    DESTINATION_CITY_WAS_FOUND_CORRECTLY("/destination_city_was_found_correctly"),
    SUBSCRIBE_TO_TICKET("/subscribe_to_ticket"),
    SUBSCRIPTION_IS_ISSUED("/subscription_is_issued"),
    UNSUBSCRIBE_FROM_TICKET("/unsubscribe_from_ticket"),
    SUBSCRIPTION_CANCELLED("/subscription_cancelled");

    private String nameCommand;

    Command(String nameCommand) {
        this.nameCommand = nameCommand;
    }

    @Override
    public String toString() {
        return nameCommand;
    }
}
