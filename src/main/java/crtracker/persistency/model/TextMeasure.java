package crtracker.persistency.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Michael Lieshoff
 */
@Table(name = "measure_texts")
@Entity
public class TextMeasure {

    @EmbeddedId
    private MeasureId measureId;

    @Column(name = "value")
    protected String value;

    public MeasureId getMeasureId() {
        return measureId;
    }

    public void setMeasureId(MeasureId measureId) {
        this.measureId = measureId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TextMeasure{" +
                "measureId=" + measureId +
                ", value='" + value + '\'' +
                '}';
    }

}
