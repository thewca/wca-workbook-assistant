package org.worldcubeassociation.ui;

import org.worldcubeassociation.db.Competition;

import javax.swing.*;
import java.awt.*;

/**
 * Renders a competition as a text label with:
 * - the start and end date
 * - the name
 * - the delegates
 * - the competition ID
 */
public class CompetitionListCellRenderer extends DefaultListCellRenderer {

    private static final String[] MONTH_LABELS = new String[]{
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec",
    };

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Competition competition = (Competition) value;
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<body>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<td valign='top' width='85px'>");
        html.append(competition.getYear());
        html.append(" ");
        if (competition.getMonth() == competition.getEndMonth()) {
            html.append(monthToText(competition.getMonth()));
            html.append(" ");
            html.append(competition.getDay());
            if (competition.getDay() != competition.getEndDay()) {
                html.append("-");
                html.append(competition.getEndDay());
            }
        }
        else {
            html.append(monthToText(competition.getMonth()));
            html.append(" ");
            html.append(competition.getDay());
            html.append(" - ");
            html.append(monthToText(competition.getEndMonth()));
            html.append(" ");
            html.append(competition.getEndDay());
        }
        html.append("</td>");
        html.append("<td>");
        html.append("<b>");
        html.append(competition.getName());
        html.append("</b>");
        html.append("<br/>");
        html.append(competition.getCountryId());
        html.append(", ");

        String[] delegates = competition.getWcaDelegate().split("\\[\\{");
        for (String delegate : delegates) {
            html.append(delegate.split("\\}")[0]);
            html.append(" ");
        }

        html.append("(");
        html.append(competition.getId());
        html.append(")");
        html.append("</td>");
        html.append("</tr>");
        html.append("</body>");
        html.append("</html>");

        return super.getListCellRendererComponent(list, html, index, isSelected, cellHasFocus);
    }

    private String monthToText(int aMonth) {
        if (aMonth >= 1 && aMonth <= 12) {
            return MONTH_LABELS[aMonth - 1];
        }
        else {
            return "?";
        }
    }

}
