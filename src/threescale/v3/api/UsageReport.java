package threescale.v3.api;

/**
 * User: geoffd
 * Date: 15/02/2013
 */
public class UsageReport {

    private String metric = "";
    private String period = "";
    private String periodStart = "";
    private String periodEnd = "";
    private String currentValue = "";
    private String maxValue = "";

    public UsageReport(String metric, String period, String periodStart, String periodEnd, String currentValue, String maxValue) {
        this.metric = metric;
        this.period = period;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
    }

    public String getMetric() {
        return metric;
    }

    public String getPeriod() {
        return period;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public boolean hasExceeded() {
        return false;
    }
}
