package net.rostex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatState {
    public String state;
    public String data = null;
    public Map<String, List<Integer>> expenses = new HashMap<>();

    public ChatState(String state) {
        this.state = state;
    }

    public String getFormattedCategories() {
        Set<String> categories = expenses.keySet();
        if (categories.isEmpty()) return "Категории отсутствует";
        return String.join("\n", categories);
    }

    public String getFormattedExpenses() {
        if (expenses.isEmpty()) return "Категории отсутствует";

        String formattedResult = "";
        for (var category : expenses.entrySet()) {
            String categoryName = category.getKey();
            List<Integer> categoryExpenses = category.getValue();
            formattedResult += categoryName + ": " + getFormattedExpenses(categoryExpenses) + "\n";
        }
        return formattedResult;

    }

    private Integer getFormattedExpenses(List<Integer> expensesPerCategory) {
        Integer formattedResult = 0;
        for (var expense : expensesPerCategory) {
            formattedResult += expense;
        }
        return formattedResult;
    }

}
