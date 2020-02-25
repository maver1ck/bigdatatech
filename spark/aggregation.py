from pyspark.sql import SparkSession
from pyspark.sql.types import StructType, StructField, DoubleType

# initializing Spark
spark = SparkSession.builder.appName("BMI Aggregator").getOrCreate()
spark.sparkContext.setLogLevel('WARN')

# read from Kafka
df = spark \
  .readStream \
  .format("kafka") \
  .option("kafka.bootstrap.servers", "localhost:9092") \
  .option("subscribe", "bmi2") \
  .load()


# extract data from json
# PUT your column name here
values = df.selectExpr('from_json(CAST(value AS STRING), "__column_name__ DOUBLE").__column__name__ as __column_name__')

# TODO: Aggregation needs to be added here

# This will be needed when running aggregation
# .outputMode("complete") 

query = values \
    .writeStream \
    .format("console") \
    .start()

query.awaitTermination()