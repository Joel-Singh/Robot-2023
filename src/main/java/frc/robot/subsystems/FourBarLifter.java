package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class FourBarLifter extends SubsystemBase {
  private CANSparkMax leftMotor;
  private CANSparkMax rightMotor;
  private MotorControllerGroup motors;

  private RelativeEncoder leftEncoder;
  private RelativeEncoder rightEncoder;

  public FourBarLifter() {
    createMotors();
    configureEncoders();
    setUpShuffleboard();
  }

  public void setMotorSpeed(double speed) {
    motors.set(speed);
  }

  private void createMotors() {
    final int LEFT_ID = 3;
    final int RIGHT_ID = 8;
    leftMotor = new CANSparkMax(LEFT_ID, MotorType.kBrushless);
    rightMotor = new CANSparkMax(RIGHT_ID, MotorType.kBrushless);
    motors = new MotorControllerGroup(leftMotor, rightMotor);

    final boolean MOTORS_IS_INVERTED = true;
    final boolean LEFT_IS_INVERTED = true;
    final boolean RIGHT_IS_INVERTED = false;

    motors.setInverted(MOTORS_IS_INVERTED);
    leftMotor.setInverted(LEFT_IS_INVERTED);
    rightMotor.setInverted(RIGHT_IS_INVERTED);

    // COAST IS USED BECAUSE A BANG BANG CONTROLLER
    // IS USED TO CONTROL FOUR BAR LIFT
    leftMotor.setIdleMode(IdleMode.kCoast);
    rightMotor.setIdleMode(IdleMode.kCoast);
  }

  private void configureEncoders() {
    final double SET_TO_EXTENSION_PERCENTAGE = -1/2.404;
    leftEncoder = leftMotor.getEncoder();
    rightEncoder = rightMotor.getEncoder();

    leftEncoder.setPositionConversionFactor(SET_TO_EXTENSION_PERCENTAGE);
    rightEncoder.setPositionConversionFactor(SET_TO_EXTENSION_PERCENTAGE);

    leftEncoder.setPosition(0);
    rightEncoder.setPosition(0);
  }


  private void setUpShuffleboard() {
    ShuffleboardTab fourBarLift = Shuffleboard.getTab("Four Bar Lifter");
    fourBarLift.addDouble("Get percentage up", this::getPercentageUp);
    fourBarLift.addDouble("Motor Power", motors::get);
    fourBarLift.addDouble("Left Motor Encoder", leftEncoder::getPosition);
    fourBarLift.addDouble("Right Motor Encoder", rightEncoder::getPosition);
  }

  public double getPercentageUp() {
    return (leftEncoder.getPosition() + rightEncoder.getPosition()) / 2;
  }
}

