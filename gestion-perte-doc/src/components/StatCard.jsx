import React from "react";
import { Card, CardContent, Typography } from "@mui/material";

const StatCard = ({ title, value, icon, color = "#1976d2" }) => {
  return (
    <Card
      style={{
        border: `2px solid black`, // Contour entier en noir
        borderRadius: 8, // Optionnel : coins arrondis
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      }}
    >
      <CardContent style={{ display: "flex", alignItems: "center" }}>
        {/* Icône colorée */}
        <div style={{ marginRight: 16, fontSize: 32, color }}>{icon}</div>
        
        {/* Texte */}
        <div>
          <Typography variant="subtitle2" color="textSecondary">
            {title}
          </Typography>
          <Typography variant="h5" fontWeight="bold" style={{ color }}>
            {value}
          </Typography>
        </div>
      </CardContent>
    </Card>
  );
};

export default StatCard;
