package frc.robot;

import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.ScheduleCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

import static edu.wpi.first.wpilibj2.command.Commands.*;
import static frc.robot.InlineCommands.*;
import static frc.robot.Test.*;

import java.util.function.Supplier;

public class RobotContainer {
  private final boolean IS_SEMI_AUTONOMOUS = true;
  private final SendableChooser<CommandBase> autoChooser = new SendableChooser<>();
  CommandScheduler commandScheduler = CommandScheduler.getInstance();


  public RobotContainer() {
    configureBindings(IS_SEMI_AUTONOMOUS);
    addTestsToShuffleboard();
    configureAutoChooser();
  }

  CommandXboxController c = controller;
  private void configureBindings(boolean isSemiAutonomous) {
    clearAllButtonBindings();
    drivetrain.setDefaultCommand(teleopDriveArcadeDrive());

    CommandBase cancelAllCommands = runOnce(() -> commandScheduler.cancelAll());
    // bind cancel all commands
    // usbButton.onTrue(cancelAllCommands);

    if (isSemiAutonomous)
      bindSemiAutonomous();
    else
      bindManualButtons();
  }

  private void bindManualButtons() {
    c.x().whileTrue(keepGrabberOpen());

    c.y().whileTrue(liftUp());
    c.a().whileTrue(liftDown());

    c.b().toggleOnTrue(brakesOn());

    c.povUp().whileTrue(telescopeForward());
    c.povDown().whileTrue(telescopeBackward());
  }

  private void bindSemiAutonomous() {
    Trigger startButton = c.start();
    Trigger backButton = c.back();

    CommandBase brakesOn = brakesOn();
    startButton.onTrue(runOnce(() -> brakesOn.schedule()));
    backButton.onTrue(runOnce(() -> brakesOn.cancel()));

    c.povUp().whileTrue(liftUp());
    c.povDown().whileTrue(liftDown());

    

    c.povRight().whileTrue(telescopeForward());
    c.povLeft().whileTrue(telescopeBackward());

    c.povUpRight().whileTrue(liftUp().deadlineWith(telescopeForward()));
    c.povUpLeft().whileTrue(liftUp().deadlineWith(telescopeBackward()));
    c.povDownRight().whileTrue(liftDown().deadlineWith(telescopeForward()));
    c.povDownLeft().whileTrue(liftDown().deadlineWith(telescopeBackward()));

    Trigger leftAndRightBumperNotPressed = (c.leftBumper().or(c.rightBumper())).negate();
    c.y().and(leftAndRightBumperNotPressed).toggleOnTrue(
      sequence(aimTopPeg(), liftToTop(), extensionForTop())
    );

    c.b().and(leftAndRightBumperNotPressed).toggleOnTrue(
      sequence(aimLowerPeg(), liftToMiddle(), extensionForMiddle())
    );

    c.x().and(leftAndRightBumperNotPressed).toggleOnTrue(
      sequence(aimAprilTag(), liftToTop(), extensionForTop())
    );

    c.a().and(leftAndRightBumperNotPressed).toggleOnTrue(
      sequence(aimAprilTag(), liftToMiddle(), extensionForMiddle())
    );

    c.y().and(c.leftBumper()).toggleOnTrue(aimTopPeg());
    c.y().and(c.rightBumper()).toggleOnTrue(liftToTop());

    c.x().and(c.leftBumper()).toggleOnTrue(aimAprilTag());
    c.x().and(c.rightBumper()).toggleOnTrue(liftToTop());

    c.a().and(c.leftBumper()).toggleOnTrue(aimAprilTag());
    c.a().and(c.rightBumper()).toggleOnTrue(liftToMiddle());

    c.b().and(c.rightBumper()).toggleOnTrue(liftToMiddle());
    c.b().and(c.leftBumper()).toggleOnTrue(aimLowerPeg());

    CommandBase rightTriggerFalseCommand =
      waitSeconds(0.5)
      .andThen(extensionBackIn())
      .andThen(liftToBottom()).withName("Right trigger false command");

    c.rightTrigger().and(leftAndRightBumperNotPressed).whileTrue(keepGrabberOpen()).onFalse(rightTriggerFalseCommand);
    // Have one on right trigger and both bumpers
    c.rightTrigger().and(c.leftBumper()) .whileTrue(grabGamepiece(0.2, 0.2)).onFalse(rightTriggerFalseCommand);
    // Bottom line doesn't work for some reason?
    c.rightTrigger().and(c.rightBumper()).whileTrue(grabGamepiece(0.3, 0.3)).onFalse(rightTriggerFalseCommand);
    c.rightTrigger().onFalse(rightTriggerFalseCommand);

    Supplier<Command> goIntoNeutral = () -> sequence(extensionBackIn(), liftToBottom());

    Trigger hasGamepiece = new Trigger(() -> grabber.hasGamepiece());
    c.leftTrigger().and(hasGamepiece)
      .toggleOnTrue(
        dropGamepiece().andThen(goIntoNeutral.get())
      );

    c.leftTrigger().and((hasGamepiece.negate())).toggleOnTrue(goIntoNeutral.get());
  }

  //TODO: Code the switching of autos with a selector on shuffleboard
  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }

  public void clearAllButtonBindings() {
    commandScheduler.getActiveButtonLoop().clear();
  }

  private void configureAutoChooser() {
    autoChooser.setDefaultOption("Place gamepiece at top", Autos.placeGamepiece.get());
    Shuffleboard.getTab("Driver").add(autoChooser);
  }


  private CommandBase grabGamepiece(double liftHeight, double extension) {
    return sequence(
      race(
        keepGrabberOpen(),
        liftToPosition(liftHeight)
      ),
      new ScheduleCommand(
        telescopeArm.setPositionCommand(extension).withName("Grab gamepiece set position command")
      ),
      keepGrabberOpen()
    );
  }
}
