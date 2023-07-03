// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {
  private XboxController controller;

  // Drivetrain
  private Drivetrain drivetrain;
  private boolean driveSlow = false;
  // Auto 
  private Autonomous auto;
  private double state;
  private String autoSelected;
  private final SendableChooser<String> autoChooser = new SendableChooser<>();
  private static final String defaultAuto = "Default";
  private static final String ChargeStation = "Charge Station";

  // Slew rate limiters to make joystick inputs more gentle; 1/3 sec from 0 to 1.
  private final SlewRateLimiter m_xspeedLimiter = new SlewRateLimiter(3);
  private final SlewRateLimiter m_yspeedLimiter = new SlewRateLimiter(3);
  private final SlewRateLimiter m_rotLimiter = new SlewRateLimiter(3);

  @Override
  public void robotInit() {
    controller = new XboxController(0);
    drivetrain = new Drivetrain();
    auto = new Autonomous(drivetrain);

    // SmartDashboard
    SmartDashboard.putData("Gyro", drivetrain.navx);
      //Auto
    SmartDashboard.putData("Auto Chooser", autoChooser);
    autoChooser.setDefaultOption("Default Auto", defaultAuto);
    autoChooser.addOption("Charge Station", ChargeStation);
  }

  @Override
  public void robotPeriodic() {
    if (controller.getStartButtonPressed()) {
      if (driveSlow) {
        driveSlow = false;
        Drivetrain.kMaxSpeed = 3; // m/s
      } else {
        driveSlow = true;
        Drivetrain.kMaxSpeed = 0.5; // m/s
      }
    }

    SmartDashboard.putBoolean("Drive Slow", driveSlow);
    SmartDashboard.putNumber("Robot Pitch", drivetrain.robotPitch());
    SmartDashboard.putNumber("Robot Roll", drivetrain.robotRoll());
  }

  @Override
  public void autonomousInit() {
    autoSelected = autoChooser.getSelected();
    System.out.println("Auto selected: " + autoSelected);
    drivetrain.resetEncoder();
    drivetrain.resetGyro();
    SmartDashboard.putNumber("Auto State", state);
    state = 0;
  }

  @Override
  public void autonomousPeriodic() {
    drivetrain.updateOdometry();
    
    // This code assumes 1000 is 1 meter
    switch (autoSelected) {
      case ChargeStation:
        if (state == 0) {
          if (drivetrain.getDistanceAVG() < 3000) {
            auto.driveStraight(1);
          } else {
            auto.driveOff();
            state = 1;
          }
        } else if (state == 1) {
          if (drivetrain.getDistanceAVG() < 4000) {
            auto.crabDrive(0.5);
          } else {
            auto.driveOff();
            state = 2;
          }
        } else if (state == 2) {
          if (Math.abs(drivetrain.navx.getRoll()) < 9) {
            auto.driveStraight(-1.0);
          } else {
            state = 3;
            auto.driveOff();
          }
        } else if (state == 3) {
          auto.chargeStation();
        } else {
          auto.driveOff();
        }
        break;
      case defaultAuto:
      default:
        auto.driveOff();
        break;
    }
  }

  @Override
  public void teleopPeriodic() {
    driveWithXbox(true);
  }

  private void driveWithXbox(boolean fieldRelative) {
    // Get the x speed. We are inverting this because Xbox controllers return
    // negative values when we push forward.
    final var xSpeed =
        -m_xspeedLimiter.calculate(MathUtil.applyDeadband(controller.getLeftY(), 0.02))
            * Drivetrain.kMaxSpeed;

    // Get the y speed or sideways/strafe speed. We are inverting this because
    // we want a positive value when we pull to the left. Xbox controllers
    // return positive values when you pull to the right by default.
    final var ySpeed =
        -m_yspeedLimiter.calculate(MathUtil.applyDeadband(controller.getLeftX(), 0.02))
            * Drivetrain.kMaxSpeed;

    // Get the rate of angular rotation. We are inverting this because we want a
    // positive value when we pull to the left (remember, CCW is positive in
    // mathematics). Xbox controllers return positive values when you pull to
    // the right by default.
    final var rot =
        -m_rotLimiter.calculate(MathUtil.applyDeadband(controller.getRightX(), 0.02))
            * Drivetrain.kMaxAngularSpeed;

    drivetrain.drive(xSpeed, ySpeed, rot, fieldRelative);
  }
}
