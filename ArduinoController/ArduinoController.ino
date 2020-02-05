  /*Three useful functions are defined:
    setupArdumoto() -- Setup the Ardumoto Shield pins
    driveArdumoto([motor], [direction], [speed]) -- Drive [motor] 
      (0 for A, 1 for B) in [direction] (0 or 1) at a [speed]
      between 0 and 255. It will spin until told to stop.
    stopArdumoto([motor]) -- Stop driving [motor] (0 or 1).
  setupArdumoto() is called in the setup().
  The loop() demonstrates use of the motor driving functions.
*/

// Clockwise and counter-clockwise definitions.
// Depending on how you wired your motors, you may need to swap.
#define FORWARD_A 0
#define REVERSE_A 1

#define FORWARD_B 1
#define REVERSE_B 0


// Motor definitions to make life easier:
#define MOTOR_A 0 // left motor
#define MOTOR_B 1 // right motor

//Uultrasonic Definitions
#define Ultra_FrontUltra 0 // Front Top sensor
#define Ultra_RightUltra 1 // Rear Top Sensor
#define Ultra_LeftUltra 2 // Front Bottom Sensor
#define Ultra_RearUltra 3 // Rear Bottom Sensor

// Pin Assignments //
//Default pins:
#define DIRA 2 // Direction control for motor A
#define PWMA 3  // PWM control (speed) for motor A
#define DIRB 4 // Direction control for motor B
#define PWMB 5 // PWM control (speed) for motor B

#define trigPin_FrontUltra 6    // Trigger for Front Sensor
#define echoPin_FrontUltra 7    // Echo for Front Sensor

#define trigPin_RearUltra 8    // Trigger for Back Sensor
#define echoPin_RearUltra 9    // Echo for Back Sensor

#define trigPin_LeftUltra 10    // Trigger for Left Sensor
#define echoPin_LeftUltra 11    // Echo for Left Sensor

#define trigPin_RightUltra 12    // Trigger for Right Sensor
#define echoPin_RightUltra 13    // Echo for Right Sensor

//Variables
bool IsMoving_Forward = false;
bool IsMoving_Reverse = false;
bool IsMoving_Left = false;
bool IsMoving_Right = false;

long duration;
long distance;
int speed = 45;
int Turnspeed = 50;
int brake = 35;
int TurnBrake = 0;
int MaxDistance = 60;
int MaxTurnDistance = 30;

//Function definitions
void MotorLogic (int val);
void driveArdumoto(byte motor, byte dir, byte spd);
void setupArdumoto();
void stopArdumoto(byte motor);




void setup()
{
  setupArdumoto(); // Set all pins as outputs
  Serial.begin(9600);
  // built in LED for testing
  pinMode(LED_BUILTIN, OUTPUT);

  pinMode(trigPin_FrontUltra, OUTPUT);
  pinMode(echoPin_FrontUltra, INPUT);

    pinMode(trigPin_RearUltra, OUTPUT);
  pinMode(echoPin_RearUltra, INPUT);

    pinMode(trigPin_LeftUltra, OUTPUT);
  pinMode(echoPin_LeftUltra, INPUT);

    pinMode(trigPin_RightUltra, OUTPUT);
  pinMode(echoPin_RightUltra, INPUT);
  
}

void loop(){

//Get move command
  
 if (Serial.available()){    // if serial value is available
  int val = Serial.read();

    //If moving forward, check sensor first
    if (val == 'w'){
      if ( Get_Distance(Ultra_FrontUltra) >= MaxDistance){
          MotorLogic ('w');   
      }
    }
  
      //If moving left, check sensor first
    if (val == 'a'){
      if ( Get_Distance(Ultra_LeftUltra) >= MaxTurnDistance){
          MotorLogic ('a');   
      }
    }
  
      //If moving right, check sensor first
    if (val == 'd'){
      if ( Get_Distance(Ultra_RightUltra) >= MaxTurnDistance){
          MotorLogic ('d');   
      }
    }
  
      //If moving back, check sensor first
    if (val == 's'){
      if ( Get_Distance(Ultra_RearUltra) >= MaxDistance){
          MotorLogic ('s');   
      }
    }

    //If stop
    if (val =='x'){
      MotorLogic('x');
    }
 }
 

    //if you are moving, check the sensors
    if (IsMoving_Forward == true){
      distance = Get_Distance(Ultra_FrontUltra);

      //Check if we have enough distance to move ahead
      if (distance <= MaxDistance){
         int val='x';
        MotorLogic(val);  //Stop the Motors
      }
      
    }

    if (IsMoving_Reverse == true){
      distance = Get_Distance(Ultra_RearUltra);

      //Check if we have enough distance to move ahead
      if (distance <= MaxDistance){
        MotorLogic('x');  //Stop the Motors
      }
      
   }

       if (IsMoving_Left == true){
      distance = Get_Distance(Ultra_LeftUltra);

      //Check if we have enough distance to move ahead
      if (distance <= MaxTurnDistance){
        MotorLogic('x');  //Stop the Motors
      }
      
   }

       if (IsMoving_Right == true){
      distance = Get_Distance(Ultra_RightUltra);

      //Check if we have enough distance to move ahead
      if (distance <= MaxTurnDistance){
        MotorLogic('x');  //Stop the Motors
      }
      
   }

    
}

// driveArdumoto drives 'motor' in 'dir' direction at 'spd' speed
void driveArdumoto(byte motor, byte dir, byte spd)
{
  if (motor == MOTOR_A)
  {
    digitalWrite(DIRA, dir);
    analogWrite(PWMA, spd);
  }
  else if (motor == MOTOR_B)
  {
    digitalWrite(DIRB, dir);
    analogWrite(PWMB, spd);
  }  
}

// stopArdumoto makes a motor stop
void stopArdumoto(byte motor)
{
  driveArdumoto(motor, 0, 0);
}

// setupArdumoto initialize all pins
void setupArdumoto()
{
  // All pins should be setup as outputs:
  pinMode(PWMA, OUTPUT);
  pinMode(PWMB, OUTPUT);
  pinMode(DIRA, OUTPUT);
  pinMode(DIRB, OUTPUT);

  // Initialize all pins as low:
  digitalWrite(PWMA, LOW);
  digitalWrite(PWMB, LOW);
  digitalWrite(DIRA, LOW);
  digitalWrite(DIRB, LOW);
}

void MotorLogic (int val){

if (val=='w'){

          IsMoving_Forward = true;
          IsMoving_Reverse = false;
          IsMoving_Left = false;
          IsMoving_Right = false;

          driveArdumoto(MOTOR_A, FORWARD_A, speed);
          driveArdumoto(MOTOR_B, FORWARD_B, speed);
          digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
          //delay(100);                       // wait for a second
          digitalWrite(LED_BUILTIN, LOW);   // turn the LED off by making the voltage LOW
        
  
          
     
  } 
   if (val=='a'){

          IsMoving_Forward = false;
          IsMoving_Reverse = false;
          IsMoving_Left = true;
          IsMoving_Right = false;
          
          driveArdumoto(MOTOR_A, FORWARD_A, Turnspeed);
          driveArdumoto(MOTOR_B, REVERSE_B, Turnspeed);
          digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
          //delay(100);                       // wait for a second
          digitalWrite(LED_BUILTIN, LOW);   // turn the LED off by making the voltage LOW
   
          
          
  } 
  
   if (val=='s'){

          IsMoving_Forward = false;
          IsMoving_Reverse = true;
          IsMoving_Left = false;
          IsMoving_Right = false;
          
          driveArdumoto(MOTOR_A, REVERSE_A, speed);
          driveArdumoto(MOTOR_B, REVERSE_B, speed);
          digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
          //delay(100);                       // wait for a second
          digitalWrite(LED_BUILTIN, LOW);   // turn the LED off by making the voltage LOW
        
          
  } 
  
   if (val=='d'){

          IsMoving_Forward = false;
          IsMoving_Reverse = false;
          IsMoving_Left = false;
          IsMoving_Right = true;
          
          driveArdumoto(MOTOR_A, REVERSE_A, Turnspeed);
          driveArdumoto(MOTOR_B, FORWARD_B, Turnspeed);
          digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
          //delay(100);                       // wait for a second
          digitalWrite(LED_BUILTIN, LOW);   // turn the LED off by making the voltage LOW
          
    } 
  
  
    if (val == 'x'){


        //If we were previously moving forward
        if (IsMoving_Forward == true){
          //Drive motors in reverse for a short period of time to slow down
          driveArdumoto(MOTOR_A, REVERSE_A, brake);
          driveArdumoto(MOTOR_B, REVERSE_B, brake);
          delay(500);
        }

          if (IsMoving_Reverse == true){
          //Drive motors in reverse for a short period of time to slow down
          driveArdumoto(MOTOR_A, FORWARD_A, brake);
          driveArdumoto(MOTOR_B, FORWARD_B, brake);
          delay(500);
        }

          if (IsMoving_Left == true){
          //Drive motors in reverse for a short period of time to slow down
          driveArdumoto(MOTOR_A, REVERSE_A, TurnBrake);
          driveArdumoto(MOTOR_B, FORWARD_B, brake);
          delay(500);
        }

          if (IsMoving_Right == true){
          //Drive motors in reverse for a short period of time to slow down
          driveArdumoto(MOTOR_A, FORWARD_A, brake);
          driveArdumoto(MOTOR_B, REVERSE_B, TurnBrake);
          delay(500);
        } 

        IsMoving_Forward = false;
        IsMoving_Reverse = false;
        IsMoving_Left = false;
        IsMoving_Right = false;
        
       driveArdumoto(MOTOR_A, REVERSE_A, 0);
       driveArdumoto(MOTOR_B, REVERSE_B, 0);
       digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on (HIGH is the voltage level)
       delay(100);                       // wait for a second
       digitalWrite(LED_BUILTIN, LOW);   // turn the LED off by making the voltage LOW
      }
      
  
}


long Get_Distance(byte sensor){

int trigPin;
int echoPin;

//Choose sensor
  if (sensor == Ultra_FrontUltra){
    trigPin = trigPin_FrontUltra;
    echoPin = echoPin_FrontUltra;
  }
  else if (sensor == Ultra_RearUltra){
    trigPin = trigPin_RearUltra;
    echoPin = echoPin_RearUltra;
  }
   else if (sensor == Ultra_LeftUltra){
    trigPin = trigPin_LeftUltra;
    echoPin = echoPin_LeftUltra;
  }
  else if (sensor == Ultra_RightUltra){
    trigPin = trigPin_RightUltra;
    echoPin = echoPin_RightUltra;
  }


// The sensor is triggered by a HIGH pulse of 10 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  digitalWrite(trigPin, LOW);
  delayMicroseconds(5);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
 
  // Read the signal from the sensor: a HIGH pulse whose
  // duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  pinMode(echoPin, INPUT);
  duration = pulseIn(echoPin, HIGH);
 
  // Convert the time into a distance
long cm = (duration/2) / 29.1;     // Divide by 29.1 or multiply by 0.034

  delay(50);
  return cm;
}
