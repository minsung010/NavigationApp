# Emergency App UI/UX Redesign Plan

## Goal Description
Enhance the visual appeal and usability of the application used by ambulance drivers and users. The goal is to make it look professional, trustworthy, and easy to use in high-stress situations (emergency dispatch).

## Proposed Changes

### 1. Color Palette & Typography (`res/values/colors.xml`, `res/values/themes.xml`)
- **Primary Color**: Emergency Red (`#D32F2F`) for "Dispatch/Action" buttons.
- **Secondary Color**: Calming Blue (`#1976D2`) or Neutral Grey for secondary actions.
- **Background**: Off-white (`#F5F5F5`) for better eye comfort.
- **Surface**: White (`#FFFFFF`) for cards.
- **Status Colors**: Active Green (`#388E3C`), Inactive Grey (`#757575`).

### 2. Driver Interface (`activity_main.xml`)
- **Layout**: Switch to a vertical linear flow or weighted ConstraintLayout to remove hardcoded sizes (`400dp`).
- **Status Card**: Encapsulate the Timer and Feedback text in a `MaterialCardView` with elevation.
- **Buttons**:
    - "Dispatch" (Start): Large, prominent red FAB or block button with an icon (Siren).
    - "Stop" (End): Secondary outlined or grey button to prevent accidental clicks, or a distinct "Stop" button that requires long-press (optional, but for now just visual distinction).
    - "My Location": Smaller tertiary button.

### 3. User Map Interface (`activity_location.xml`)
- **Overlays**:
    - "Current Location" text should be a floating Card at the top or bottom with rounded corners and shadow, not a plain background.
    - Zoom buttons: Replace with standard Google Maps UI settings or style them as floating mini-buttons.
- **Feedback**: Add a "Status Chip" or Banner that appears when the ambulance is dispatching (reading from the `isDispatching` logic added previously).

## Verification Plan
### Manual Verification
1.  **Visual Check**: Build the app and verify the new colors and layout alignment on different screen sizes (simulated).
2.  **Interaction**: Check press states (ripple effects) on buttons.
3.  **Readability**: Ensure text contrast is high and fonts are legible.
