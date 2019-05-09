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

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 * @author Michael Lieshoff
 */
@Embeddable
public class MeasureId implements Serializable {

    private long hash;

    private Date modifiedAt;

    private int type;

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeasureId measureId = (MeasureId) o;

        if (hash != measureId.hash) return false;
        if (type != measureId.type) return false;
        return modifiedAt != null ? modifiedAt.equals(measureId.modifiedAt) : measureId.modifiedAt == null;
    }

    @Override
    public int hashCode() {
        int result = (int) hash;
        result = 31 * result + (modifiedAt != null ? modifiedAt.hashCode() : 0);
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "MeasureId{" +
                "hash=" + hash +
                ", modifiedAt=" + modifiedAt +
                ", type=" + type +
                '}';
    }
    
}
