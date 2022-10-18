package org.dbpedia.databus.utils;

import java.time.LocalDate;

public class LocalDateFilterRange {


    public LocalDate startDate;
    public LocalDate endDate;

    public LocalDateFilterRange(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public boolean dateInRange(LocalDate date) {

        // if date is null it is never in the range
        if (date == null) {
            return false;
        }

        // here we go if a date is actually given
        if (this.startDate != null && this.endDate != null) {
            // if both set -> must be after start and before end
            return date.isAfter(this.startDate) && date.isBefore(this.endDate);
        } else if (this.startDate == null && this.endDate != null) {
            return date.isBefore(this.endDate);
        } else if (this.startDate != null) {
            return date.isAfter(this.startDate);
        } else {
            // if both not set -> everything is in range
            return true;
        }
    }

    public boolean overlapsRange(LocalDate startDate, LocalDate endDate) {
        return this.dateInRange(startDate) || this.dateInRange(endDate);
    }
}
