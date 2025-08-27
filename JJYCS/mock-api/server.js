const express = require('express');
const cors = require('cors');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = 8081;
const JWT_SECRET = 'mock-jwt-secret';

// Middleware
app.use(cors());
app.use(express.json());

// Mock users database  
const users = [
    {
        id: 1,
        email: 'kimcs@email.com',
        password: 'password', // simplified for mock
        name: 'Kim Cheolsu',
        userType: 'GENERAL',
        status: 'ACTIVE'
    },
    {
        id: 2,
        email: 'lee@company.com',
        password: 'password', // simplified for mock
        name: 'Lee Younghee',
        userType: 'CORPORATE',
        status: 'ACTIVE'
    },
    {
        id: 3,
        email: 'park@partner.com',
        password: 'password', // simplified for mock
        name: 'Park Minsu',
        userType: 'PARTNER',
        status: 'ACTIVE'
    },
    {
        id: 4,
        email: 'admin@ycs.com',
        password: 'password', // simplified for mock
        name: 'Administrator',
        userType: 'ADMIN',
        status: 'ACTIVE'
    }
];

// Health check
app.get('/api/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Login endpoint
app.post('/api/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                error: 'Email and password are required'
            });
        }

        // Find user
        const user = users.find(u => u.email === email);
        if (!user) {
            return res.status(401).json({
                success: false,
                error: 'Invalid email or password'
            });
        }

        // Check password (simplified for mock server - just check if password is 'password')
        if (password !== 'password') {
            return res.status(401).json({
                success: false,
                error: 'Invalid email or password'
            });
        }

        // Generate JWT token
        const token = jwt.sign(
            { 
                userId: user.id, 
                email: user.email, 
                userType: user.userType 
            },
            JWT_SECRET,
            { expiresIn: '24h' }
        );

        // Return success response
        res.json({
            success: true,
            token: token,
            accessToken: token,
            user: {
                id: user.id,
                email: user.email,
                name: user.name,
                userType: user.userType,
                status: user.status
            }
        });

    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({
            success: false,
            error: 'Internal server error'
        });
    }
});

// Register endpoint
app.post('/api/auth/signup', async (req, res) => {
    try {
        const { email, password, name, userType } = req.body;

        if (!email || !password || !name) {
            return res.status(400).json({
                success: false,
                error: 'Email, password and name are required'
            });
        }

        // Check if user already exists
        const existingUser = users.find(u => u.email === email);
        if (existingUser) {
            return res.status(409).json({
                success: false,
                error: 'User with this email already exists'
            });
        }

        // Hash password
        const hashedPassword = await bcrypt.hash(password, 10);

        // Create new user
        const newUser = {
            id: users.length + 1,
            email,
            password: hashedPassword,
            name,
            userType: userType || 'GENERAL',
            status: 'ACTIVE'
        };

        users.push(newUser);

        res.json({
            success: true,
            message: 'User registered successfully',
            user: {
                id: newUser.id,
                email: newUser.email,
                name: newUser.name,
                userType: newUser.userType,
                status: newUser.status
            }
        });

    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({
            success: false,
            error: 'Internal server error'
        });
    }
});

// Logout endpoint
app.post('/api/auth/logout', (req, res) => {
    res.json({
        success: true,
        message: 'Logged out successfully'
    });
});

// Start server
app.listen(PORT, () => {
    console.log(`Mock API server running on http://localhost:${PORT}`);
    console.log('Available endpoints:');
    console.log('  GET  /api/health');
    console.log('  POST /api/auth/login');
    console.log('  POST /api/auth/signup');
    console.log('  POST /api/auth/logout');
    console.log('\nTest users:');
    console.log('  kimcs@email.com / password (GENERAL)');
    console.log('  lee@company.com / password (CORPORATE)');
    console.log('  park@partner.com / password (PARTNER)');
    console.log('  admin@ycs.com / password (ADMIN)');
});