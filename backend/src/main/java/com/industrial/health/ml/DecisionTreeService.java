package com.industrial.health.ml;

import com.industrial.health.config.HealthProperties;
import com.industrial.health.model.SensorRecord;
import com.industrial.health.repo.SensorRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import smile.classification.DecisionTree;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 决策树训练 + 预测服务，基于 Smile ML 的 CART 实现。
 */
@Service
public class DecisionTreeService {

    private final SensorRecordRepository recordRepo;
    private final HealthProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile DecisionTree model;
    private volatile Map<Integer, String> reverseLabel = new HashMap<>();
    private volatile double accuracy;
    private volatile int trainSize;
    private volatile int testSize;
    private volatile double[] featureImportance = new double[0];
    private volatile long lastTrainAt;

    public DecisionTreeService(SensorRecordRepository recordRepo, HealthProperties props) {
        this.recordRepo = recordRepo;
        this.props = props;
    }

    @PostConstruct
    public void init() {
        props.getLabelMapping().forEach((k, v) -> reverseLabel.put(v, k));
    }

    public synchronized TrainResult train() throws Exception {
        List<SensorRecord> all = recordRepo.findAll();
        if (all.size() < 10) {
            throw new IllegalStateException("训练样本不足（至少 10 条）");
        }

        List<String> feats = props.getFeatureColumns();
        int n = all.size();
        int f = feats.size();

        double[][] x = new double[n][f];
        int[] y = new int[n];

        Map<String, Integer> mapping = props.getLabelMapping();

        for (int i = 0; i < n; i++) {
            SensorRecord r = all.get(i);
            Map<String, Object> row = mapper.readValue(r.getFeaturesJson(), Map.class);
            for (int j = 0; j < f; j++) {
                Object v = row.get(feats.get(j));
                x[i][j] = v == null ? 0.0 : Double.parseDouble(v.toString());
            }
            Integer cls = mapping.get(r.getLabel());
            y[i] = cls == null ? 0 : cls;
        }

        // shuffle + split 8:2
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Collections.shuffle(Arrays.asList(idx), new Random(42));
        int trainN = (int) (n * 0.8);
        double[][] xTrain = new double[trainN][f];
        int[] yTrain = new int[trainN];
        double[][] xTest = new double[n - trainN][f];
        int[] yTest = new int[n - trainN];
        for (int i = 0; i < n; i++) {
            if (i < trainN) { xTrain[i] = x[idx[i]]; yTrain[i] = y[idx[i]]; }
            else { xTest[i - trainN] = x[idx[i]]; yTest[i - trainN] = y[idx[i]]; }
        }

        // 构建 DataFrame
        StructField[] fields = new StructField[f + 1];
        for (int j = 0; j < f; j++) fields[j] = new StructField(feats.get(j), DataTypes.DoubleType);
        String[] labelNames = mapping.keySet().toArray(new String[0]);
        fields[f] = new StructField("label", DataTypes.IntegerType, new NominalScale(labelNames));

        DataFrame trainDf = buildDataFrame(xTrain, yTrain, feats, fields);
        DataFrame testDf  = buildDataFrame(xTest,  yTest,  feats, fields);

        Formula formula = Formula.lhs("label");
        DecisionTree dt = DecisionTree.fit(formula, trainDf);

        // 评估
        int correct = 0;
        int[] pred = new int[yTest.length];
        for (int i = 0; i < xTest.length; i++) {
            pred[i] = dt.predict(testDf.get(i));
            if (pred[i] == yTest[i]) correct++;
        }
        this.accuracy = xTest.length == 0 ? 1.0 : (double) correct / xTest.length;
        this.trainSize = trainN;
        this.testSize = xTest.length;
        this.featureImportance = dt.importance();
        this.model = dt;
        this.lastTrainAt = System.currentTimeMillis();

        return info();
    }

    private DataFrame buildDataFrame(double[][] x, int[] y, List<String> feats, StructField[] fields) {
        int n = x.length;
        int f = feats.size();
        smile.data.vector.BaseVector[] vectors = new smile.data.vector.BaseVector[f + 1];
        for (int j = 0; j < f; j++) {
            double[] col = new double[n];
            for (int i = 0; i < n; i++) col[i] = x[i][j];
            vectors[j] = DoubleVector.of(fields[j], col);
        }
        vectors[f] = IntVector.of(fields[f], y);
        return DataFrame.of(vectors);
    }

    public PredictResult predict(Map<String, Double> features) {
        if (model == null) throw new IllegalStateException("模型尚未训练");
        List<String> feats = props.getFeatureColumns();
        int f = feats.size();

        StructField[] fields = new StructField[f + 1];
        for (int j = 0; j < f; j++) fields[j] = new StructField(feats.get(j), DataTypes.DoubleType);
        String[] labelNames = props.getLabelMapping().keySet().toArray(new String[0]);
        fields[f] = new StructField("label", DataTypes.IntegerType, new NominalScale(labelNames));

        double[][] x = new double[1][f];
        for (int j = 0; j < f; j++) {
            Double v = features.get(feats.get(j));
            x[0][j] = v == null ? 0.0 : v;
        }
        DataFrame df = buildDataFrame(x, new int[]{0}, feats, fields);

        double[] posteriori = new double[reverseLabel.size()];
        int cls = model.predict(df.get(0), posteriori);
        String labelName = reverseLabel.getOrDefault(cls, String.valueOf(cls));
        double conf = posteriori[cls];
        return new PredictResult(labelName, conf, posteriori);
    }

    public TrainResult info() {
        Map<String, Double> imp = new LinkedHashMap<>();
        List<String> feats = props.getFeatureColumns();
        for (int j = 0; j < feats.size() && j < featureImportance.length; j++) {
            imp.put(feats.get(j), featureImportance[j]);
        }
        return new TrainResult(model != null, accuracy, trainSize, testSize, imp, lastTrainAt);
    }

    public boolean isTrained() { return model != null; }

    public record TrainResult(boolean trained, double accuracy, int trainSize, int testSize,
                              Map<String, Double> featureImportance, long lastTrainAt) {}
    public record PredictResult(String label, double confidence, double[] probabilities) {}
}
