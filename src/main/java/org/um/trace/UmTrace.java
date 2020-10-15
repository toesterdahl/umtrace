package org.um.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author toesterdahl
 *
 */
public class UmTrace implements Comparable<UmTrace> {

  final List<Node> path;

  final Map<String, String> prefixesMap;
  
  final Map<UmTrace, Double> aggregateTime;
  
  final static ThreadLocal<Map<String, UmTrace>> tl = new ThreadLocal<Map<String, UmTrace>>();
  
  public UmTrace() {
    this.path = Collections.unmodifiableList(new ArrayList<Node>());
    this.prefixesMap = Collections.unmodifiableMap(new HashMap<String, String>());
    this.aggregateTime = new TreeMap<UmTrace, Double>();
  }

  public UmTrace(Map<String,String> prefixesMap, Map<UmTrace, Double> aggregateTime) {
    this.path = Collections.unmodifiableList(new ArrayList<Node>());
    this.prefixesMap = Collections.unmodifiableMap(prefixesMap);
    this.aggregateTime = aggregateTime;
  }

  public UmTrace(List<Node> path, Map<String,String> prefixesMap, Map<UmTrace, Double> aggregateTime) {
    this.path = Collections.unmodifiableList(path);
    this.prefixesMap = Collections.unmodifiableMap(prefixesMap);
    this.aggregateTime = aggregateTime;
  }

  public UmTrace push(String localName) {
    if (localName == null) {
      throw new NullPointerException("localName is not allowed to be null");
    }

    if (localName.length() == 0) {
      throw new IllegalStateException("localName is not allowed to be zero length");
    }

    if (path.size() > 200) {
      System.out.println("UMTRACE: path exceeding limit (200). Does it not reach the exit?");
      System.out.println("UMTRACE: stacktrace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return this;
    }
    
    List<Node> newPath = new ArrayList<Node>(path);
    newPath.add(new Node(null, localName));

    return new UmTrace(newPath, prefixesMap, aggregateTime);
  }

  public UmTrace appendPathElement(String localName, String nameSpaceURI) {
    if (localName == null) {
      throw new NullPointerException("localName is not allowed to be null");
    }

    if (localName.length() == 0) {
      throw new IllegalStateException("localName is not allowed to be zero length");
    }

    if (path.size() > 200) {
      System.out.println("UMTRACE: path exceeding limit (200). Does it not reach the exit?");
      System.out.println("UMTRACE: stacktrace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
      return this;
    }
    
    List<Node> newPath = new ArrayList<Node>(path);
    newPath.add(new Node(nameSpaceURI, localName));

    return new UmTrace(newPath, prefixesMap, aggregateTime);
  }

  public UmTrace pop() {
    Node current = path.get(path.size() - 1);
    Double currentAggregate = aggregateTime.get(this);
    double d = (System.currentTimeMillis() - current.ts) / 1000.0;
    double newAggregate = currentAggregate==null?d:currentAggregate + d;
    aggregateTime.put(this, newAggregate);
    
    List<Node> newPath = new ArrayList<Node>(path);
    newPath.remove(path.size() - 1);
    UmTrace doTrace = new UmTrace(newPath, prefixesMap, aggregateTime);
    
    // Print the aggregates, unasked, when popping the first element. 
    if (path.size() <= 1) {
      doTrace.printAggregateSummary();
    }
    return doTrace;
  }

  public void printAggregateSummary() {
    Double total = null;
    ArrayList<UmTrace> sortedList = new ArrayList<UmTrace>(aggregateTime.keySet());
    Collections.sort(sortedList);
    
    for (UmTrace n: sortedList) {
      Double aggregate = aggregateTime.get(n);
      if (total == null) {
        // List is ordered, the first item should be the total.  
        total = aggregate;
      }
      System.out.println(String.format("UMTRACE Aggregates. Node: %s Time: %f s. Pct: %,.2f%%", n.toString(), aggregate, (aggregate / total) * 100));
    }
  }
  
  public int depth() {
    return path.size();
  }
  
  public String getLocalName() {
    return path.get(path.size() - 1).localName;
  }
  
  @Override
  public int compareTo(UmTrace o) {
    if (o == null) {
      // Better be explicit
      throw new NullPointerException();
    } else if (this == o) {
      // Check if this is actually the same object
      return 0;
    } else if (this.path.size() != o.path.size()) {
      // shorter path is sorted before the longer path
      return this.path.size() - o.path.size();
    } else {
      // we know we have to path of equal length,
      // we compare item by item
      for (int i = 0; i < path.size(); i++) {
        int c = path.get(i).compareTo(o.path.get(i));
        if (c != 0) {
          return c;
        }
      }
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof UmTrace)) {
      return false;
    }
    return 0 == this.compareTo((UmTrace) o);
  }

  @Override
  public int hashCode() {
    int result = path.size() << 16;
    for (int i = 0; i < path.size(); i++) {
      Node n = path.get(i);
      result = 7 * result + n.hashCode();
    }
    
    return result;
  }

  class Node implements Comparable<Node> {

    public Node(String localName) {
      this.nameSpaceURI = null;
      this.localName = localName;
      this.ts = System.currentTimeMillis();
    }

    public Node(String nameSpaceURI, String localName) {
      this.nameSpaceURI = nameSpaceURI;
      this.localName = localName;
      this.ts = System.currentTimeMillis();
    }

    final String nameSpaceURI;
    final String localName;
    final long ts;

    @Override
    public int compareTo(Node o) {
      if (nameSpaceURI != null && !nameSpaceURI.equals(o.nameSpaceURI)) {
        return nameSpaceURI.compareTo(o.nameSpaceURI);
      } else {
        return localName.compareTo(o.localName);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (!(o instanceof Node)) {
        return false;
      }
      return 0 == this.compareTo((Node) o);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((nameSpaceURI == null) ? 0 : nameSpaceURI.hashCode());
      result = prime * result + ((localName == null) ? 0 : localName.hashCode());
      return result;
    }
    
    @Override
    public String toString() {
      // nameSpaceName can take the values nameSpacePrefix, URI or null
      String nameSpaceName = nameSpaceURI != null && prefixesMap.containsKey(nameSpaceURI) ? prefixesMap.get(nameSpaceURI) : nameSpaceURI;
      
      
      StringBuilder builder = new StringBuilder();
      builder.append("/");
      if (nameSpaceName != null) {
        builder.append(nameSpaceName);
        builder.append(":");
      }
      builder.append(localName);
      return builder.toString();
    }

  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("UmTrace [path=");
    for (Node n : path) {
      builder.append(n.toString());
    }
    builder.append("]");
    return builder.toString();
  }
  
  public static Map<String, UmTrace> getTl() {
    Map<String, UmTrace> instance = tl.get();
    if (instance == null) {
      instance = new HashMap<String, UmTrace>();
      tl.set(instance);
    }
    return instance;
  }
  
  public static UmTrace getInstance(String ctx) {
    UmTrace dt = getTl().get(ctx);
    if (dt != null) {
      return dt;
    }
    dt = new UmTrace();
    getTl().put(ctx, dt);
    return dt;
  }
  
  public static void clearAll() {
    getTl().clear();
  }
  
  public static void enter(String ctx, String section) {
    UmTrace dot = getInstance(ctx);
    dot = dot.appendPathElement(section, ctx);
    getTl().put(ctx, dot);
  }
  
  public static void exit(String ctx) {
    UmTrace dot = getInstance(ctx);
    dot = dot.pop();
    getTl().put(ctx, dot);
  }
  
  public static void enter(String section) {
    UmTrace dot = getInstance("default");
    dot = dot.push(section);
    getTl().put("default", dot);
  }
  
  public static void exit() {
    UmTrace dot = getInstance("default");
    dot = dot.pop();
    getTl().put("default", dot);
  }
  
}
