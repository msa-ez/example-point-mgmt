package OnePoint.controller;

public class PointLackException extends Exception {

  PointLackException() {
    System.err.println("포인트가 부족합니다.");
  }
}
