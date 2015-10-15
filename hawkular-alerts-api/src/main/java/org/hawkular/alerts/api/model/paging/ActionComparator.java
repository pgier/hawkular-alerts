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
package org.hawkular.alerts.api.model.paging;

import java.util.Comparator;

import org.hawkular.alerts.api.model.action.Action;

/**
 *
 * @author Lucas Ponce
 */
public class ActionComparator implements Comparator<Action> {

    public enum Field {
        ACTION_PLUGIN("actionPlugin"),
        ACTION_ID("actionId"),
        ALERT_ID("alertId"),
        CTIME("ctime"),
        RESULT("result");

        private String text;

        Field(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public static Field getField(String text) {
            if (text == null || text.isEmpty()) {
                return ALERT_ID;
            }
            for (Field f : values()) {
                if (f.getText().compareToIgnoreCase(text) == 0) {
                    return f;
                }
            }
            return ALERT_ID;
        }
    };

    private Field field;
    private Order.Direction direction;

    public ActionComparator() {
        this(Field.ALERT_ID, Order.Direction.ASCENDING);
    }

    public ActionComparator(Field field, Order.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
    @Override
    public int compare(Action o1, Action o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return 1;
        }
        if (o1 != null && o2 == null) {
            return -1;
        }
        int iOrder = direction == Order.Direction.ASCENDING ? 1 : -1;
        switch (field) {
            case ALERT_ID:
                if (o1.getAlert() == null && o2.getAlert() == null) {
                    return 0;
                }
                if (o1.getAlert() == null && o2.getAlert() != null) {
                    return 1;
                }
                if (o1.getAlert() != null && o2.getAlert() == null) {
                    return -1;
                }
                if (o1.getAlert().getAlertId() == null && o2.getAlert().getAlertId() == null) {
                    return 0;
                }
                if (o1.getAlert().getAlertId() == null && o2.getAlert().getAlertId() != null) {
                    return 1;
                }
                if (o1.getAlert().getAlertId() != null && o2.getAlert().getAlertId() == null) {
                    return -1;
                }
                return o1.getAlert().getAlertId().compareTo(o2.getAlert().getAlertId()) * iOrder;
            case ACTION_PLUGIN:
                if (o1.getActionPlugin() == null && o2.getActionPlugin() == null) {
                    return 0;
                }
                if (o1.getActionPlugin() == null && o2.getActionPlugin() != null) {
                    return 1;
                }
                if (o1.getActionPlugin() != null && o2.getActionPlugin() == null) {
                    return -1;
                }
                return o1.getActionPlugin().compareTo(o2.getActionPlugin()) * iOrder;
            case CTIME:
                return (int)((o1.getCtime() - o2.getCtime()) * iOrder);
            case ACTION_ID:
                if (o1.getActionId() == null && o2.getActionId() == null) {
                    return 0;
                }
                if (o1.getActionId() == null && o2.getActionId() != null) {
                    return 1;
                }
                if (o1.getActionId() != null && o2.getActionId() == null) {
                    return -1;
                }
                return o1.getActionId().compareTo(o2.getActionId()) * iOrder;
            case RESULT:
                if (o1.getResult() == null && o2.getResult() == null) {
                    return 0;
                }
                if (o1.getResult() == null && o2.getResult() != null) {
                    return 1;
                }
                if (o1.getResult() != null && o2.getResult() == null) {
                    return -1;
                }
                return o1.getResult().compareTo(o2.getResult()) * iOrder;
        }
        return 0;
    }
}