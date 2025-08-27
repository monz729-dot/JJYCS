const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 8081;

// Enable CORS for all routes
app.use(cors({
  origin: ['http://localhost:3001', 'http://127.0.0.1:3001'],
  credentials: true
}));

app.use(express.json());

// Mock HS Code search by product name
app.get('/api/hscode/search/by-product', (req, res) => {
  const { productName } = req.query;
  
  console.log(`Searching HS Code for product: ${productName}`);
  
  // Mock search results
  const mockResults = [
    {
      hsCode: "850431",
      koreanName: "전자 변압기",
      englishName: "Electronic transformers",
      unit: "개",
      basicRate: 8.0,
      wtoRate: 0.0,
      specialRate: 0.0
    },
    {
      hsCode: "640399",
      koreanName: "기타 가죽신발",
      englishName: "Other leather footwear",
      unit: "켤레",
      basicRate: 13.0,
      wtoRate: 0.0,
      specialRate: 0.0
    }
  ];

  const filteredResults = mockResults.filter(item => 
    item.koreanName.includes(productName) || 
    item.englishName.toLowerCase().includes(productName.toLowerCase())
  );

  res.json({
    success: true,
    message: "HS Code search completed",
    items: filteredResults
  });
});

// Mock HS Code search by HS Code
app.get('/api/hscode/search/by-hscode', (req, res) => {
  const { hsCode } = req.query;
  
  console.log(`Searching product name for HS Code: ${hsCode}`);
  
  // Mock result
  const mockResult = {
    hsCode: hsCode,
    koreanName: "전자제품 (샘플)",
    englishName: "Electronic device (sample)",
    unit: "개",
    basicRate: 8.0,
    wtoRate: 0.0,
    specialRate: 0.0
  };

  res.json({
    success: true,
    message: "Product name search completed",
    items: [mockResult]
  });
});

// Mock tariff rate lookup
app.get('/api/hscode/tariff/:hsCode', (req, res) => {
  const { hsCode } = req.params;
  
  console.log(`Getting tariff rate for HS Code: ${hsCode}`);
  
  // Mock tariff data
  const mockTariff = {
    hsCode: hsCode,
    koreanName: "전자제품 (샘플)",
    englishName: "Electronic device (sample)",
    basicRate: 8.0,
    wtoRate: 0.0,
    specialRate: 0.0,
    unit: "개"
  };

  res.json({
    success: true,
    message: "Tariff rate lookup completed",
    items: [mockTariff]
  });
});

// Mock exchange rate
app.get('/api/hscode/exchange-rate', (req, res) => {
  console.log("Getting tariff exchange rate");
  
  // Mock exchange rate data
  const mockExchangeRate = {
    baseCurrency: "USD",
    targetCurrency: "KRW",
    exchangeRate: 1350.0,
    lastUpdated: new Date().toISOString()
  };

  res.json({
    success: true,
    message: "Exchange rate lookup completed",
    data: mockExchangeRate
  });
});

// Mock duty calculation
app.post('/api/hscode/calculate-duty', (req, res) => {
  const { hsCode, quantity, value, currency } = req.body;
  
  console.log(`Calculating customs duty for HS Code: ${hsCode}`);
  
  // Mock duty calculation
  const mockCalculation = {
    hsCode: hsCode,
    productName: "전자제품 (샘플)",
    basicRate: 8.0,
    wtoRate: 0.0,
    specialRate: 0.0,
    appliedRate: 8.0,
    dutyAmount: value * 0.08, // 8% duty
    totalAmount: value * 1.08
  };

  res.json(mockCalculation);
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'Mock HS Code API Server is running' });
});

app.listen(PORT, () => {
  console.log(`Mock HS Code API Server running on http://localhost:${PORT}`);
  console.log(`CORS enabled for frontend at http://localhost:3001`);
});