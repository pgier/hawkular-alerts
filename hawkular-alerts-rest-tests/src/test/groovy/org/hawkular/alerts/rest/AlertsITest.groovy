/**
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
package org.hawkular.alerts.rest

import static org.junit.Assert.assertEquals

import org.junit.Test

/**
 * Alerts REST tests.
 *
 * @author Lucas Ponce
 */
class AlertsITest extends AbstractITestBase {

    @Test
    void findAlerts() {
        def resp = client.get(path: "")
        assert resp.status == 200 || resp.status == 204 : resp.status
    }

    @Test
    void findAlertsByCriteria() {
        String now = String.valueOf(System.currentTimeMillis());
        def resp = client.get(path: "", query: [endTime:now, startTime:"0",triggerIds:"Trigger-01,Trigger-02"] )
        assert resp.status == 200 || resp.status == 204 : resp.status

        resp = client.get(path: "", query: [endTime:now, startTime:"0",alertIds:"Trigger-01|"+now+","+"Trigger-02|"+now] )
        assert resp.status == 200 || resp.status == 204 : resp.status

        resp = client.get(path: "", query: [endTime:now, startTime:"0",statuses:"OPEN,ACKNOWLEDGED,RESOLVED"] )
        assert resp.status == 200 || resp.status == 204 : resp.status

        resp = client.get(path: "", query: [tags:"data-01,data-02"] )
        assert resp.status == 200 || resp.status == 204 : resp.status

        resp = client.get(path: "", query: [tags:"dataId|data-01,dataId|data-02"] )
        assert resp.status == 200 || resp.status == 204 : resp.status
    }

    @Test
    void reloadTest() {
        def resp = client.get(path: "reload")
        assertEquals(200, resp.status)
    }

}
