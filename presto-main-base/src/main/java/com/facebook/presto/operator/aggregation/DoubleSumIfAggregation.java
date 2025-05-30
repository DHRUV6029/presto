/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator.aggregation;

import com.facebook.presto.common.block.BlockBuilder;
import com.facebook.presto.common.type.StandardTypes;
import com.facebook.presto.operator.aggregation.state.NullableDoubleState;
import com.facebook.presto.spi.function.AggregationFunction;
import com.facebook.presto.spi.function.AggregationState;
import com.facebook.presto.spi.function.CombineFunction;
import com.facebook.presto.spi.function.InputFunction;
import com.facebook.presto.spi.function.OutputFunction;
import com.facebook.presto.spi.function.SqlType;

import static com.facebook.presto.common.type.DoubleType.DOUBLE;

@AggregationFunction(value = "sum_if", isCalledOnNullInput = true)
public final class DoubleSumIfAggregation
{
    private DoubleSumIfAggregation() {}

    @InputFunction
    public static void input(@AggregationState NullableDoubleState state, @SqlType(StandardTypes.BOOLEAN) boolean value,
                             @SqlType(StandardTypes.DOUBLE) double sum)
    {
        if (value) {
            state.setNull(false);
            state.setDouble(state.getDouble() + sum);
        }
    }

    @InputFunction
    public static void input(@AggregationState NullableDoubleState state, @SqlType(StandardTypes.BOOLEAN) boolean value,
                             @SqlType(StandardTypes.DOUBLE) double sum, @SqlType(StandardTypes.DOUBLE) double defaultValue)
    {
        state.setNull(false);
        if (value) {
            state.setDouble(state.getDouble() + sum);
        }
        else {
            state.setDouble(state.getDouble() + defaultValue);
        }
    }

    @CombineFunction
    public static void combine(@AggregationState NullableDoubleState state, @AggregationState NullableDoubleState otherState)
    {
        if (state.isNull()) {
            if (otherState.isNull()) {
                return;
            }
            state.setNull(false);
            state.setDouble(otherState.getDouble());
            return;
        }

        if (!otherState.isNull()) {
            state.setDouble(state.getDouble() + otherState.getDouble());
        }
    }

    @OutputFunction(StandardTypes.DOUBLE)
    public static void output(@AggregationState NullableDoubleState state, BlockBuilder out)
    {
        if (state.isNull()) {
            out.appendNull();
        }
        else {
            DOUBLE.writeDouble(out, state.getDouble());
        }
    }
}
