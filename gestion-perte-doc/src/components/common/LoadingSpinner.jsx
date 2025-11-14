import React from 'react';
import { Spinner } from 'react-bootstrap';

const LoadingSpinner = () => {
  return (
    <div className="d-flex justify-content-center align-items-center" style={{ height: '200px' }}>
      <Spinner animation="border" role="status">
        <span className="visually-hidden">Chargement...</span>
      </Spinner>
    </div>
  );
};

export default LoadingSpinner;