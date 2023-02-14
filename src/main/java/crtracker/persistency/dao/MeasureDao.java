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

package crtracker.persistency.dao;

import com.google.common.hash.Hashing;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import crtracker.persistency.model.DecimalMeasure;
import crtracker.persistency.model.MeasureId;
import crtracker.persistency.model.MeasureType;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;

@Service
public class MeasureDao {

  public StringMeasure updateStringMeasure(Session session, String id, int measureType, String value, Date modifiedAt) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateStringMeasure(session, hash, measureType, value, modifiedAt);
  }

  public StringMeasure updateStringMeasure(Session session, String id, int measureType, String value) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateStringMeasure(session, hash, measureType, value, new Date());
  }

  private StringMeasure saveId(Session session, String id, long hash) {
    return internUpdateStringMeasure(session, hash, -1, id, new Date());
  }

  private StringMeasure internUpdateStringMeasure(Session session, long hash, int measureType, String value,
                                                  Date modifiedAt) {
    MeasureId measureId = new MeasureId();
    measureId.setHash(hash);
    measureId.setModifiedAt(modifiedAt);
    measureId.setType(measureType);

    StringMeasure measure = new StringMeasure();
    measure.setValue(value);
    measure.setMeasureId(measureId);

    List<StringMeasure> old = (List<StringMeasure>) session.createQuery("from " + StringMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by modifiedAt desc")
        .setLong("hash", measureId.getHash()).setInteger("type", measureId.getType()).setMaxResults(1).list();

    StringMeasure first = null;
    boolean change = false;

    if (CollectionUtils.isNotEmpty(old)) {
      first = old.get(0);
      change = ObjectUtils.compare(first.getValue(), value) != 0;
      if (!change) {
        first = null;
      }
    }

    if (CollectionUtils.isEmpty(old) || change) {
      session.save(measure);
    }

    return first;
  }

  public NumberMeasure updateNumberMeasure(Session session, String id, int measureType, long value) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateNumberMeasure(session, hash, measureType, value, new Date());
  }

  public NumberMeasure updateNumberMeasure(Session session, String id, int measureType, long value, Date modifiedAt) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateNumberMeasure(session, hash, measureType, value, modifiedAt);
  }

  private NumberMeasure internUpdateNumberMeasure(Session session, long hash, int measureType, long value,
                                                  Date date) {
    MeasureId measureId = new MeasureId();
    measureId.setHash(hash);
    measureId.setModifiedAt(date);
    measureId.setType(measureType);

    NumberMeasure measure = new NumberMeasure();
    measure.setValue(value);
    measure.setMeasureId(measureId);

    List<NumberMeasure> old = (List<NumberMeasure>) session.createQuery("from " + NumberMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by modifiedAt desc")
        .setLong("hash", measureId.getHash()).setInteger("type", measureId.getType()).setMaxResults(1).list();

    NumberMeasure first = null;
    boolean change = false;

    if (CollectionUtils.isNotEmpty(old)) {
      first = old.get(0);
      change = ObjectUtils.compare(first.getValue(), value) != 0;
      if (!change) {
        first = null;
      }
    }

    if (CollectionUtils.isEmpty(old) || change) {
      session.save(measure);
    }

    return first;
  }

  public DecimalMeasure updateDecimalMeasure(Session session, String id, int measureType, double value) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateDecimalMeasure(session, hash, measureType, value, null);
  }

  public DecimalMeasure updateDecimalMeasure(Session session, String id, int measureType, double value,
                                             Date modifiedAt) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateDecimalMeasure(session, hash, measureType, value, modifiedAt);
  }

  private DecimalMeasure internUpdateDecimalMeasure(Session session, long hash, int measureType, double value,
                                                    Date modifiedAt) {
    MeasureId measureId = new MeasureId();
    measureId.setHash(hash);
    measureId.setModifiedAt(modifiedAt);
    measureId.setType(measureType);

    DecimalMeasure measure = new DecimalMeasure();
    measure.setValue(value);
    measure.setMeasureId(measureId);

    List<DecimalMeasure> old = (List<DecimalMeasure>) session.createQuery("from " + DecimalMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by t.measureId.modifiedAt desc")
        .setLong("hash", measureId.getHash()).setInteger("type", measureId.getType()).setMaxResults(1).list();

    DecimalMeasure first = null;
    boolean change = false;

    if (CollectionUtils.isNotEmpty(old)) {
      first = old.get(0);
      change = ObjectUtils.compare(first.getValue(), value) != 0;
      if (!change) {
        first = null;
      }
    }

    if (CollectionUtils.isEmpty(old) || change) {
      session.save(measure);
    }

    return first;
  }

  public TextMeasure updateTextMeasure(Session session, String id, int measureType, String value, Date modifiedAt) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateTextMeasure(session, hash, measureType, value, modifiedAt);
  }

  public TextMeasure updateTextMeasure(Session session, String id, int measureType, String value) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    saveId(session, id, hash);
    return internUpdateTextMeasure(session, hash, measureType, value, new Date());
  }

  private TextMeasure internUpdateTextMeasure(Session session, long hash, int measureType, String value,
                                              Date modifiedAt) {
    MeasureId measureId = new MeasureId();
    measureId.setHash(hash);
    measureId.setModifiedAt(modifiedAt);
    measureId.setType(measureType);

    TextMeasure measure = new TextMeasure();
    measure.setValue(value);
    measure.setMeasureId(measureId);

    List<TextMeasure> old = (List<TextMeasure>) session.createQuery("from " + TextMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by modifiedAt desc")
        .setLong("hash", measureId.getHash()).setInteger("type", measureId.getType()).setMaxResults(1).list();

    TextMeasure first = null;
    boolean change = false;

    if (CollectionUtils.isNotEmpty(old)) {
      first = old.get(0);
      change = ObjectUtils.compare(first.getValue(), value) != 0;
      if (!change) {
        first = null;
      }
    }

    if (CollectionUtils.isEmpty(old) || change) {
      session.save(measure);
    }

    return first;
  }

  public StringMeasure getCurrentStringMeasure(Session session, MeasureType measureType, String id) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return (StringMeasure) session.createQuery("from " + StringMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by t.measureId.modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setMaxResults(1).uniqueResult();
  }

  public List<StringMeasure> getLastStringMeasures(Session session, MeasureType measureType, String id, int limit) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return session.createQuery("from " + StringMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by t.measureId.modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setMaxResults(limit).list();
  }

  public List<TextMeasure> getLastTextMeasures(Session session, MeasureType measureType, String id, int limit) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return session.createQuery("from " + TextMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by t.measureId.modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setMaxResults(limit).list();
  }

  public NumberMeasure getCurrentNumberMeasure(Session session, MeasureType measureType, String id) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return (NumberMeasure) session.createQuery("from " + NumberMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setMaxResults(1).uniqueResult();
  }

  public TextMeasure getCurrentTextMeasure(Session session, MeasureType measureType, String id) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return (TextMeasure) session.createQuery("from " + TextMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by t.measureId.modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setMaxResults(1).uniqueResult();
  }

  public List<NumberMeasure> getLastNumberMeasures(Session session, MeasureType measureType, String id, int limit) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return session.createQuery("from " + NumberMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type order by modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setMaxResults(limit).list();
  }

  public NumberMeasure getLastNumberMeasure(Session session, MeasureType measureType, String id, DateTime from,
                                            DateTime to) {
    long hash = Hashing.md5().hashString(id, StandardCharsets.UTF_8).asLong();
    return (NumberMeasure) session.createQuery("from " + NumberMeasure.class.getName()
        + " t where t.measureId.hash=:hash and t.measureId.type=:type and modifiedAt>=:from and modifiedAt<=:to order by modifiedAt desc")
        .setLong("hash", hash).setInteger("type", measureType.getCode()).setTimestamp("from", from.toDate())
        .setTimestamp("to", to.toDate()).setMaxResults(1).uniqueResult();
  }

  public Long getCountStringMeasures(Session session) {
    return ((BigInteger) session.createSQLQuery("select count(*) from measure_strings").uniqueResult()).longValue();
  }

  public Long getCountNumberMeasures(Session session) {
    return ((BigInteger) session.createSQLQuery("select count(*) from measure_numbers").uniqueResult()).longValue();
  }

  public Long getCountDecimalMeasures(Session session) {
    return ((BigInteger) session.createSQLQuery("select count(*) from measure_decimals").uniqueResult())
        .longValue();
  }

  public Long getCountTextMeasures(Session session) {
    return ((BigInteger) session.createSQLQuery("select count(*) from measure_texts").uniqueResult()).longValue();
  }

}
