#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"
 
const int mpuAddress = 0x68;  // Puede ser 0x68 o 0x69
MPU6050 mpu(mpuAddress);
 
int ax, ay, az;
int gx, gy, gz;
  
// Factores de conversion
const float accScale = 2.0 * 9.81 / 32768.0;
const float gyroScale = 250.0 / 32768.0;

int xPin = A0;
int yPin = A1;
int buttonPin = 4;
int xVal;
int yVal;
int buttonState;

void setup() {
  Serial.begin(9600);
  pinMode(xPin, INPUT);
  pinMode(yPin, INPUT);
  pinMode(buttonPin, INPUT_PULLUP);
  Wire.begin();
  mpu.initialize();
}

void loop() {
  xVal = analogRead(xPin);
  yVal = analogRead(yPin);
  buttonState = digitalRead(buttonPin);
  mpu.getAcceleration(&ax, &ay, &az);
  mpu.getRotation(&gx, &gy, &gz);

  Serial.print(xVal);
  Serial.print(",");
  Serial.print(yVal);
  Serial.print(",");
  Serial.print(buttonState); 
  Serial.print(",");
  Serial.print(ax * accScale);
  Serial.print(",");
  Serial.print(ay * accScale);
  Serial.print(",");
  Serial.print(az * accScale);
  Serial.print(",");
  Serial.print(gx * gyroScale);
  Serial.print(",");
  Serial.print(gy * gyroScale);
  Serial.print(",");
  Serial.println(gz * gyroScale);
  
  delay(100);
}
