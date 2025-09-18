@echo off
echo Setting up Git repository for ENDEcode Modern UI...

REM Initialize git if not already done
if not exist .git (
    git init
    echo Git repository initialized.
) else (
    echo Git repository already exists.
)

REM Add all files
git add .

REM Create commit with modern UI changes
git commit -m "🎨 Complete Modern UI Redesign with Glassmorphism

Features:
- Modern glassmorphism design system
- Dark theme with cyberpunk color palette  
- Animated terminal console with line numbers
- Enhanced file selector with drag & drop effects
- Glass cards with smooth animations
- Modern typography and iconography
- Responsive layout design
- All original functionality preserved

Technical:
- New GlassmorphismComponents.kt
- Updated theme system with modern colors
- Enhanced ConsoleView with terminal styling
- Improved FileSelector with animations
- Completely redesigned HomeScreen
- Spring physics animations
- Status indicators and micro-interactions"

echo.
echo Git repository is ready!
echo.
echo To push to GitHub:
echo 1. Create new repository on GitHub
echo 2. Copy the repository URL
echo 3. Run: git remote add origin YOUR_GITHUB_URL
echo 4. Run: git branch -M main
echo 5. Run: git push -u origin main
echo.
echo Example:
echo git remote add origin https://github.com/yourusername/endecode-modern.git
echo git branch -M main  
echo git push -u origin main
echo.
pause
