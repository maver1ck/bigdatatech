/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bigdata;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingJob {

	private static class AverageAggregate implements AggregateFunction<Double, Tuple2<Double, Integer>, Double> {

		@Override
		public Tuple2<Double, Integer> createAccumulator() {
			// sum, count
			return new Tuple2<>(0.0, 0);
		}

		@Override
		public Tuple2<Double, Integer> add(Double value, Tuple2<Double, Integer> accumulator) {
			// sum += value, count +=1
			return new Tuple2<>(accumulator.f0 + value, accumulator.f1 + 1);
		}

		@Override
		public Double getResult(Tuple2<Double, Integer> accumulator) {
			// if there is no accumulated values return -1
			if (accumulator.f1 == 0)
				return -1.0;
			// return avg = sum / count
			return accumulator.f0 / accumulator.f1;
		}

		@Override
		public Tuple2<Double, Integer> merge(Tuple2<Double, Integer> a, Tuple2<Double, Integer> b) {
			// merge two averages
			return new Tuple2<>(a.f0 + b.f0, a.f1 + b.f1);
		}
	}


	public static void main(String[] args) throws Exception {

		// set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// set up Kafka
		Properties kafkaProperties = new Properties();
		kafkaProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-cp-kafka:9092");
		kafkaProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "bigdata-training");
		kafkaProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		DataStreamSource<ObjectNode> bmi = env.addSource(new FlinkKafkaConsumer<>("bmi",
				new JSONKeyValueDeserializationSchema(true), kafkaProperties));
		SingleOutputStreamOperator<Double> values = bmi.map(node -> node.get("value").get("bmi").asDouble());
		values.windowAll(GlobalWindows.create()).trigger(CountTrigger.of(1)).aggregate(new AverageAggregate()).print();

		env.execute("Flink Streaming BMI aggregation");
	}
}
