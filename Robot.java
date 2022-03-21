package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {
  
  //Definitions for the hardware. Change this if you change what stuff you have plugged in
  private final Joystick m_stick = new Joystick(0);
  private final PWMVictorSPX intake = new PWMVictorSPX(5);
  private final PWMVictorSPX driveLeftA = new PWMVictorSPX(0);
  private final PWMVictorSPX driveLeftB = new PWMVictorSPX(1);
  private final PWMVictorSPX driveRightA = new PWMVictorSPX(2);
  private final PWMVictorSPX driveRightB = new PWMVictorSPX(3);
  private final PWMVictorSPX arm = new PWMVictorSPX(7);
  //Changed the arm motor PWM line to 7 from 10
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(new SpeedControllerGroup(driveLeftA, driveLeftB), new SpeedControllerGroup(driveRightA, driveRightB));
  UsbCamera camera1 = CameraServer.startAutomaticCapture();
  UsbCamera camera2 = CameraServer.startAutomaticCapture();

  // @Override
  // public void robotInit() {
  //     m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
  //     m_chooser.addOption("My Auto", kCustomAuto);
  //     SmartDashboard.putData("Auto modes", m_chooser);
  // }

  Joystick driverController = new Joystick(0);

  //Constants for controlling the arm. consider tuning these for your particular robot
  final double armHoldUp = 0.08;
  final double armHoldDown = 0.13;
  final double armTravel = 0.5;

  final double armTimeUp = 0.5;
  final double armTimeDown = 0.35;

  //Varibles needed for the code
  boolean armUp = true; //Arm initialized to up because that's how it would start a match
  boolean burstMode = false;
  double lastBurstTime = 0;

  double autoStart = 0;
  boolean goForAuto = false;


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    //Configure motors to turn correct direction. You may have to invert some of your motors
    driveLeftA.setInverted(false);
    driveLeftB.setInverted(false);
    driveRightA.setInverted(true);
    driveRightB.setInverted(true);
    
    arm.setInverted(false);

    //add a thing on the dashboard to turn off auto if needed
    SmartDashboard.putBoolean("Go For Auto", false);
    goForAuto = SmartDashboard.getBoolean("Go For Auto", false);
  }

  @Override
  public void autonomousInit() {
    //get a time for auton start to do events based on time later
    autoStart = Timer.getFPGATimestamp();
    //check dashboard icon to ensure good to do auto
    goForAuto = SmartDashboard.getBoolean("Go For Auto", false);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    //arm control code. same as in teleop
    if(armUp){
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeUp){
        arm.set(armTravel);
      }
      else{
        arm.set(armHoldUp);
      }
    }
    else{
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeDown){
        arm.set(-armTravel);
      }
      else{
        arm.set(-armHoldUp);
      }
    }
    
    //get time since start of auto
    double autoTimeElapsed = Timer.getFPGATimestamp() - autoStart;
    if(goForAuto){
      //series of timed events making up the flow of auto
      if(autoTimeElapsed < 3){
        //spit out the ball for three seconds
        intake.set(-1);
      }else if(autoTimeElapsed < 6){
        //stop spitting out the ball and drive backwards *slowly* for three seconds
        intake.set(0);
        driveLeftA.set(-0.3);
        driveLeftB.set(-0.3);
        driveRightA.set(-0.3);
        driveRightB.set(-0.3);
      }else{
        //do nothing for the rest of auto
        intake.set(0);
        driveLeftA.set(0);
        driveLeftB.set(0);
        driveRightA.set(0);
        driveRightB.set(0);
      }
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    // Drive arcade style
    //3.19.22 multiplied m_stick.getX() by -1 to invert z-axis controls - LS
    m_robotDrive.arcadeDrive(m_stick.getY()*-1, m_stick.getX());

    //Intake controls
    if(driverController.getRawButton(2)){
      intake.set(0.45);;
    }
    else if(driverController.getRawButton(1)){
      intake.set(-0.5);
    }
    else{
      intake.set(0);
    }

    //Trying to make an easier-to-understand if statement that the Everybot code - LS
    if(driverController.getRawButton(3)){
      arm.set(-0.6);
    }else if(driverController.getRawButton(4)){
      arm.set(0.25);
    }else{
      arm.set(0);
    }
    //Arm Controls
    /*
    if(armUp){
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeUp){
        arm.set(armTravel);
      }
      else{
        arm.set(armHoldUp);
      }
    }
    else{
      if(Timer.getFPGATimestamp() - lastBurstTime < armTimeDown){
        arm.set(-armTravel);
      }
      else{
        arm.set(-armHoldDown);
      }
    }
  
    if(driverController.getRawButtonPressed(7) && !armUp){
      lastBurstTime = Timer.getFPGATimestamp();
      armUp = true;
    }
    else if(driverController.getRawButtonPressed(8) && armUp){
      lastBurstTime = Timer.getFPGATimestamp();
      armUp = false;
    }  
    */

  }

  @Override
  public void disabledInit() {
    //On disable turn off everything
    //done to solve issue with motors "remembering" previous setpoints after reenable
    driveLeftA.set(0);
    driveLeftB.set(0);
    driveRightA.set(0);
    driveRightB.set(0);
    arm.set(0);
    intake.set(0);
  }
    
}