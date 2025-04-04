/*
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including
 * without limitation fees for hosting or consulting/ support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from the functionality of the
 * Software. Any license notice or attribution required by the License must also include this
 * Commons Clause License Condition notice.
 *
 * Software: Infinitic
 *
 * License: MIT License (https://opensource.org/licenses/MIT)
 *
 * Licensor: infinitic.io
 */

package io.infinitic.serDe;

import io.infinitic.common.serDe.SerializedData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SerDeJavaTest {

    @Test
    public void nullObjShouldBeSerializableDeserializable() {
        SerializedData data = SerializedData.encode(null, null, null);

        Assertions.assertNull(data.decode(null, null));
    }

    @Test
    public void simpleObjectShouldBeSerializableDeserializable() {
        Pojo1 val1 = new Pojo1("42", 42, JType.TYPE_1);
        SerializedData data = SerializedData.encode(val1, null, null);

        Assertions.assertEquals(val1, data.decode(null, null));
    }

    @Test
    public void objectShouldBeDeserializableEvenWithMoreProperties() {
        Pojo1 val1 = new Pojo1("42", 42, JType.TYPE_1);
        Pojo2 val2 = new Pojo2("42", 42);
        SerializedData original = SerializedData.encode(val1, Pojo1.class, null);
        SerializedData data = original.copy(original.toJsonString(false).replace("Pojo1", "Pojo2").getBytes(), original.getDataType(), original.getMeta());

        Assertions.assertEquals(val2, data.decode(Pojo2.class, null));
    }

    @Test
    public void objectShouldBeDeserializableEvenWithLessProperties() {
        Pojo1 val1 = new Pojo1("42", 42, JType.TYPE_1);
        Pojo2 val2 = new Pojo2("42", 42);
        SerializedData original = SerializedData.encode(val2, Pojo2.class, null);
        SerializedData data = original.copy(original.toJsonString(false).replace("Pojo2", "Pojo1").getBytes(), original.getDataType(), original.getMeta());

        Assertions.assertEquals(val1, data.decode(Pojo1.class, null));
    }

    @Test
    public void testPolymorphicDeserialize() {
        Pojo2 pojo = new Pojo2("42", 42);
        PojoWrapper val1 = new PojoWrapper(pojo, pojo);

        SerializedData data = SerializedData.encode(val1, null, null);

        Assertions.assertEquals(val1, data.decode(null, null));
    }

    @Test
    public void ListOfSimpleObjectShouldBeSerializableDeserializableWithoutType() {
        Pojo1 pojoa = new Pojo1("42", 42, JType.TYPE_1);
        Pojo1 pojob = new Pojo1("24", 24, JType.TYPE_2);
        List<Pojo1> pojos = new ArrayList<>();
        pojos.add(pojoa);
        pojos.add(pojob);
        SerializedData data = SerializedData.encode(pojos, null, null);

        Assertions.assertEquals(pojos, data.decode(null, null));
    }

    @Test
    public void ListOfSerializableObjectShouldBeSerializableDeserializableWithType() {
        Pojo1 pojoa = new Pojo1("42", 42, JType.TYPE_1);
        Pojo1 pojob = new Pojo1("24", 24, JType.TYPE_2);
        List<Pojo1> pojos = new ArrayList<>();
        pojos.add(pojoa);
        pojos.add(pojob);
        SerializedData data = SerializedData.encode(pojos, null, null);

        System.out.println(data);

        Assertions.assertEquals(pojos, data.decode(null, null));
    }
}
