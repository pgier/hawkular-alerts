/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.alerts.api.model.condition;

import org.hawkular.alerts.api.model.trigger.Mode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A threshold condition against rate of change over time. Typically used for "counter" metrics, that continuously
 * increase or decrease.  Given the last two datums for dataId:
 * <pre>
 *   deltaTime   = datum.time - prevDatum.time
 *   deltaValue  = datum.value - prevData.value
 *   periods     = deltaTime / <conditionPeriod>
 *   rate        = deltaValue / periods
 *   match       = rate <conditionOperator> <conditionThreshold>
 * </pre>
 *
 * In other words, take the rate of change for the most recent datums and compare it to the threshold. For example,
 * Let's say we have a metric, sessionCount, that increments for each new session.  If the sessionCount increases
 * too quickly, say more than 20 per minute, we want an alert.  We'd want:
 * <pre>
 *   RateCondition( 'SessionCount', INCREASING, MINUTE, GT, 20 )
 * </pre>
 *
 * By specifying the SessionCount data as increasing, we know to ignore/reset if the previous session count is
 * less than the current session count.  This indicates that maybe the counter was reset (maybe due to a restart).
 * <p>
 * Note that rate of change is always determined as an absolute value. So threshold values should be >= 0.
 * </p>
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class RateCondition extends Condition {

    private static final long serialVersionUID = 1L;

    public enum Operator {
        LT, GT, LTE, GTE
    }

    /** Default: MINUTE */
    public enum Period {
        SECOND(1000L), MINUTE(60000L), HOUR(60000L * 60), DAY(60000L * 60 * 24), WEEK(60000L * 60 * 24 * 7);

        public long milliseconds;

        Period(long milliseconds) {
            this.milliseconds = milliseconds;
        }
    }

    /** Default: INCREASING */
    public enum Direction {
        DECREASING, INCREASING, NA
    }

    @JsonInclude(Include.NON_NULL)
    private String dataId;

    @JsonInclude(Include.NON_NULL)
    private Direction direction;

    @JsonInclude(Include.NON_NULL)
    private Period period;

    @JsonInclude(Include.NON_NULL)
    private Operator operator;

    @JsonInclude(Include.NON_NULL)
    private Double threshold;

    /**
     * Used for JSON deserialization, not for general use.
     */
    public RateCondition() {
        /*
            Default constructor is needed for JSON libraries in JAX-RS context.
         */
        this("", "", Mode.FIRING, 1, 1, null, null, null, null, null);
    }

    public RateCondition(String tenantId, String triggerId, String dataId, Direction direction, Period period,
            Operator operator, Double threshold) {

        this(tenantId, triggerId, Mode.FIRING, 1, 1, dataId, direction, period, operator, threshold);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public RateCondition(String triggerId, Mode triggerMode, String dataId, Direction direction,
            Period period, Operator operator, Double threshold) {

        this("", triggerId, triggerMode, 1, 1, dataId, direction, period, operator, threshold);
    }

    public RateCondition(String tenantId, String triggerId, Mode triggerMode, String dataId, Direction direction,
            Period period, Operator operator, Double threshold) {

        this(tenantId, triggerId, triggerMode, 1, 1, dataId, direction, period, operator, threshold);
    }

    /**
     * This constructor requires the tenantId be assigned prior to persistence. It can be used when
     * creating triggers via Rest, as the tenant will be assigned automatically.
     */
    public RateCondition(String triggerId, Mode triggerMode, int conditionSetSize, int conditionSetIndex,
            String dataId, Direction direction, Period period, Operator operator, Double threshold) {

        this("", triggerId, triggerMode, conditionSetSize, conditionSetIndex, dataId, direction, period, operator,
                threshold);
    }

    public RateCondition(String tenantId, String triggerId, Mode triggerMode, int conditionSetSize,
            int conditionSetIndex,
            String dataId, Direction direction, Period period, Operator operator, Double threshold) {

        super(tenantId, triggerId, (null == triggerMode ? Mode.FIRING : triggerMode), conditionSetSize,
                conditionSetIndex, Type.RATE);
        this.dataId = dataId;
        this.direction = (null == direction) ? Direction.INCREASING : direction;
        this.period = (null == period) ? Period.MINUTE : period;
        this.operator = operator;
        this.threshold = threshold;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getLog(long time, double value, long previousTime, double previousValue) {
        long deltaTime = time - previousTime;
        double deltaValue = (Direction.INCREASING == direction) ? (value - previousValue) : (previousValue - value);
        double periods = deltaTime / period.milliseconds;
        double rate = deltaValue / periods;

        return triggerId + " : " + direction + " " + rate + " " + operator.name() + " " + threshold + " per "
                + period;
    }

    public boolean match(long time, double value, long previousTime, double previousValue) {
        double rate = getRate(time, value, previousTime, previousValue);

        if (rate < 0) {
            return false;
        }

        switch (operator) {
            case LT:
                return rate < threshold;
            case GT:
                return rate > threshold;
            case LTE:
                return rate <= threshold;
            case GTE:
                return rate >= threshold;
            default:
                throw new IllegalStateException("Unknown operator: " + operator.name());
        }
    }

    public double getRate(long time, double value, long previousTime, double previousValue) {
        double deltaTime = (double) (time - previousTime);
        double deltaValue = (Direction.INCREASING == direction) ? (value - previousValue) : (previousValue - value);
        double periods = deltaTime / period.milliseconds;
        double rate = deltaValue / periods;

        return rate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + ((period == null) ? 0 : period.hashCode());
        result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RateCondition other = (RateCondition) obj;
        if (dataId == null) {
            if (other.dataId != null)
                return false;
        } else if (!dataId.equals(other.dataId))
            return false;
        if (direction != other.direction)
            return false;
        if (operator != other.operator)
            return false;
        if (period != other.period)
            return false;
        if (threshold == null) {
            if (other.threshold != null)
                return false;
        } else if (!threshold.equals(other.threshold))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RateCondition [dataId=" + dataId + ", direction=" + direction + ", period=" + period + ", operator="
                + operator + ", threshold=" + threshold + ", tenantId=" + tenantId + ", triggerId=" + triggerId
                + ", triggerMode=" + triggerMode + "]";
    }

}
