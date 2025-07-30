package bkatwal.zookeeper.demo.controller;

import static bkatwal.zookeeper.demo.util.ZkDemoUtil.getHostPostOfServer;
import static bkatwal.zookeeper.demo.util.ZkDemoUtil.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import bkatwal.zookeeper.demo.model.Person;
import bkatwal.zookeeper.demo.util.ClusterInfo;
import bkatwal.zookeeper.demo.util.DataStorage;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;

/** @author "Bikas Katwal" 26/03/19 */
@RestController
public class ZookeeperDemoController {

  private RestTemplate restTemplate = new RestTemplate();
  private ZooKeeper zooKeeper;
  private static final String ZK_NODE = "/mlmodel";
  private Classifier model;

  @PostConstruct
  public void init() throws Exception {
      this.zooKeeper = new ZooKeeper("localhost:2181", 3000, null);

      // Ako nod ne postoji, kreiraj ga praznog
      if (zooKeeper.exists(ZK_NODE, false) == null) {
          zooKeeper.create(ZK_NODE, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }

      // Postavi watcher
      zooKeeper.exists(ZK_NODE, new Watcher() {
          @Override
          public void process(WatchedEvent event) {
              if (event.getType() == Event.EventType.NodeDataChanged) {
                  updateLocalModel();
                  try {
                      zooKeeper.exists(ZK_NODE, this); // re-register watcher
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }
          }
      });

      // Učitaj model ako postoji
      if (zooKeeper.exists(ZK_NODE, false) != null) {
          updateLocalModel();
      }
  }

  @PostMapping("/put")
  public String putModel() throws Exception {
      try {
        Classifier trainedModel = trainModelFromCSV();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trainedModel);
        oos.flush();
        byte[] modelBytes = baos.toByteArray();

        if (zooKeeper.exists(ZK_NODE, false) != null) {
            zooKeeper.setData(ZK_NODE, modelBytes, -1);
        } else {
            zooKeeper.create(ZK_NODE, modelBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        this.model = trainedModel; // lokalno čuvanje modela
        return "Model trained and pushed to ZooKeeper.";

    } catch (Exception e) {
        e.printStackTrace();
        return "Training or ZK update failed: " + e.getMessage();
    }
  }


  private Instances createTestInstance(String type,
                                        double fixedAcidity,
                                        double volatileAcidity,
                                        double citricAcid,
                                        double residualSugar,
                                        double chlorides,
                                        double freeSulfurDioxide,
                                        double totalSulfurDioxide,
                                        double density,
                                        double pH,
                                        double sulphates,
                                        double alcohol) throws Exception {
    
    CSVLoader loader = new CSVLoader();
    loader.setSource(new File("data/winequality-white.csv"));
    Instances structure = loader.getDataSet();

    structure.setClassIndex(structure.numAttributes() - 1);

    // Pretvaranje klasne promenljive u nominalnu, kao u treningu
    weka.filters.unsupervised.attribute.NumericToNominal convert = new weka.filters.unsupervised.attribute.NumericToNominal();
    convert.setAttributeIndices("first,last"); // pretvaramo 'type' (prva) i 'quality' (poslednja) promenljive u nominalne promenljive
    convert.setInputFormat(structure);
    structure = Filter.useFilter(structure, convert);

    double[] values = new double[structure.numAttributes()];
    values[0] = structure.attribute(0).indexOfValue(type);
    values[1] = fixedAcidity;
    values[2] = volatileAcidity;
    values[3] = citricAcid;
    values[4] = residualSugar;
    values[5] = chlorides;
    values[6] = freeSulfurDioxide;
    values[7] = totalSulfurDioxide;
    values[8] = density;
    values[9] = pH;
    values[10] = sulphates;
    values[11] = alcohol;

    structure.clear();
    structure.add(new weka.core.DenseInstance(1.0, values));
    return structure;
}


  @GetMapping("/get")
  public String predict(@RequestParam String type,
      @RequestParam double fixedAcidity,
      @RequestParam double volatileAcidity,
      @RequestParam double citricAcid,
      @RequestParam double residualSugar,
      @RequestParam double chlorides,
      @RequestParam double freeSulfurDioxide,
      @RequestParam double totalSulfurDioxide,
      @RequestParam double density,
      @RequestParam double pH,
      @RequestParam double sulphates,
      @RequestParam double alcohol) {
      if (model == null) {
        try {
            updateLocalModel();
        } catch (Exception e) {
            e.printStackTrace();
            return "Model not found in ZooKeeper.";
        }
        if (model == null) return "Model not found in ZooKeeper.";
    }
      try {
          Instances testData = createTestInstance(type, 
                                                  fixedAcidity, 
                                                  volatileAcidity, 
                                                  citricAcid, 
                                                  residualSugar, 
                                                  chlorides, 
                                                  freeSulfurDioxide, 
                                                  totalSulfurDioxide, 
                                                  density, 
                                                  pH, 
                                                  sulphates, 
                                                  alcohol);

          double result = model.classifyInstance(testData.firstInstance());
          return "Predicted quality: " + testData.classAttribute().value((int) result);
      } catch (Exception e) {
          e.printStackTrace();
          return "Prediction error: " + e.getMessage();
      }
  }


  private void updateLocalModel() {
      try {
          byte[] data = zooKeeper.getData(ZK_NODE, false, null);
          ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
          this.model = (Classifier) ois.readObject();
          System.out.println("Local model updated from ZooKeeper.");
      } catch (Exception e) {
          System.err.println("Failed to update local model: " + e.getMessage());
      }
  }

 
  private static Classifier trainModelFromCSV() throws Exception {
    CSVLoader loader = new CSVLoader();
    loader.setSource(new File("data/winequality-white.csv"));
    Instances data = loader.getDataSet();

    data.setClassIndex(data.numAttributes() - 1); // poslednja kolona: quality

    // konverzija u nominalnu klasnu promenljivu
    weka.filters.unsupervised.attribute.NumericToNominal convert = new weka.filters.unsupervised.attribute.NumericToNominal();
    convert.setAttributeIndices("first,last");
    convert.setInputFormat(data);
    data = Filter.useFilter(data, convert);

    Classifier classifier = new J48();  // C4.5 decision tree
    classifier.buildClassifier(data);
    return classifier;
  }

  @PutMapping("/person/{id}/{name}")
  public ResponseEntity<String> savePerson(
      HttpServletRequest request,
      @PathVariable("id") Integer id,
      @PathVariable("name") String name) {

    String requestFrom = request.getHeader("request_from");
    String leader = ClusterInfo.getClusterInfo().getMaster();
    if (!isEmpty(requestFrom) && requestFrom.equalsIgnoreCase(leader)) {
      Person person = new Person(id, name);
      DataStorage.setPerson(person);
      return ResponseEntity.ok("SUCCESS");
    }
    // If I am leader I will broadcast data to all live node, else forward request to leader
    if (amILeader()) {
      List<String> liveNodes = ClusterInfo.getClusterInfo().getLiveNodes();

      int successCount = 0;
      for (String node : liveNodes) {

        if (getHostPostOfServer().equals(node)) {
          Person person = new Person(id, name);
          DataStorage.setPerson(person);
          successCount++;
        } else {
          String requestUrl =
              "http://"
                  .concat(node)
                  .concat("person")
                  .concat("/")
                  .concat(String.valueOf(id))
                  .concat("/")
                  .concat(name);
          HttpHeaders headers = new HttpHeaders();
          headers.add("request_from", leader);
          headers.setContentType(MediaType.APPLICATION_JSON);

          HttpEntity<String> entity = new HttpEntity<>(headers);
          restTemplate.exchange(requestUrl, HttpMethod.PUT, entity, String.class).getBody();
          successCount++;
        }
      }

      return ResponseEntity.ok()
          .body("Successfully update ".concat(String.valueOf(successCount)).concat(" nodes"));
    } else {
      String requestUrl =
          "http://"
              .concat(leader)
              .concat("person")
              .concat("/")
              .concat(String.valueOf(id))
              .concat("/")
              .concat(name);
      HttpHeaders headers = new HttpHeaders();

      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> entity = new HttpEntity<>(headers);
      return restTemplate.exchange(requestUrl, HttpMethod.PUT, entity, String.class);
    }
  }

  private boolean amILeader() {
    String leader = ClusterInfo.getClusterInfo().getMaster();
    return getHostPostOfServer().equals(leader);
  }

  @GetMapping("/persons")
  public ResponseEntity<List<Person>> getPerson() {

    return ResponseEntity.ok(DataStorage.getPersonListFromStorage());
  }

  @GetMapping("/clusterInfo")
  public ResponseEntity<ClusterInfo> getClusterinfo() {

    return ResponseEntity.ok(ClusterInfo.getClusterInfo());
  }
}
